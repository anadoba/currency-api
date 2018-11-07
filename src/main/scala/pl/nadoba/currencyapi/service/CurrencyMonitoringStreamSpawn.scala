package pl.nadoba.currencyapi.service

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{KillSwitches, Materializer, SharedKillSwitch}
import pl.nadoba.currencyapi.config.CurrencyMonitoringConfig
import pl.nadoba.currencyapi.models.Currency

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CurrencyMonitoringStreamSpawn(
  ratesService: CurrencyRatesService,
  monitoringConfig: CurrencyMonitoringConfig,
  onChangeHook: CurrencyRatesChangeHook)(
  implicit materializer: Materializer, ec: ExecutionContext
) {

  def spawn(currency: Currency): SharedKillSwitch = {
    val killSwitch = KillSwitches.shared(currency.symbol)

    println(s"Started monitoring stream for currency ${currency.symbol}")

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
        println(s"Detected rates change for currency ${currency.symbol}. Calling the webhook...")
        onChangeHook.execute(currency)
      }
      .via(killSwitch.flow)
      .runWith(Sink.ignore)
      .onComplete { _ =>
        println(s"Monitoring stream for currency ${currency.symbol} closed")
      }

    killSwitch
  }

}
