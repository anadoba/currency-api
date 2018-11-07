package pl.nadoba.currencyapi.service

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import com.typesafe.scalalogging.LazyLogging
import pl.nadoba.currencyapi.config.CurrencyMonitoringConfig
import pl.nadoba.currencyapi.models.Currency

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CurrencyMonitoringStreamSpawn(
  ratesService: CurrencyRatesService,
  monitoringConfig: CurrencyMonitoringConfig,
  onChangeHook: CurrencyRatesChangeHook)(
  implicit materializer: Materializer, ec: ExecutionContext
) extends LazyLogging {

  def spawn(currency: Currency): SharedKillSwitch = {
    val killSwitch = KillSwitches.shared(currency.symbol)

    logger.debug(s"Started monitoring stream for currency ${currency.symbol}")

    Source
      .tick(initialDelay = 0.seconds, interval = monitoringConfig.interval, tick = NotUsed)
      .mapAsync(1)(_ => ratesService.getCurrencyRates(currency, None, None))
      .collect {
        case Right(ratesResponse) => ratesResponse.rates
      }
      .sliding(2)
      .filter {
        case Seq(firstRates, secondRates) =>
          firstRates != secondRates
      }
      .mapAsync(1) { _ =>
        logger.debug(s"Detected rates change for currency ${currency.symbol}. Calling the webhook...")
        onChangeHook.execute(currency)
      }
      .via(killSwitch.flow)
      .runWith(Sink.ignore)
      .onComplete { _ =>
        logger.debug(s"Monitoring stream for currency ${currency.symbol} closed")
      }

    killSwitch
  }

}
