package pl.nadoba.currencyapi

import java.time.{ZoneId, ZonedDateTime}

import akka.Done
import com.typesafe.config.ConfigFactory
import org.scalatest.mockito.MockitoSugar
import pl.nadoba.currencyapi.config.CurrencyMonitoringConfig
import pl.nadoba.currencyapi.models.{Currency, CurrencyApiErrorResponse, CurrencyRatesResponse}
import pl.nadoba.currencyapi.service.{CurrencyMonitoringStreamSpawn, CurrencyRatesChangeHook, CurrencyRatesService}

import scala.concurrent.Future
import org.mockito.Mockito._
import pl.nadoba.currencyapi.testUtils.ActorSpec

class CurrencyMonitoringStreamSpec extends ActorSpec("CurrencyMonitoringStreamSpec") with MockitoSugar {

  "Currency Monitoring Stream" should {

    "execute the currency rate change hook on currency rates change" in new Context {
      override def currencyRate: Long = System.currentTimeMillis()

      withRunningCurrencyMonitoringStream {
        verify(onChangeHook, atLeastOnce()).execute(baseCurrency)
      }
    }

    "not execute the currency rate change hook if rates are stable" in new Context {
      override def currencyRate: Long = 1234L

      withRunningCurrencyMonitoringStream {
        verifyZeroInteractions(onChangeHook)
      }
    }

  }

  trait Context {

    def currencyRate: Long

    val rateService = new CurrencyRatesService {
      override def getCurrencyRates(base: Currency, zonedDateTimeOpt: Option[ZonedDateTime], targetOpt: Option[Currency]): Future[Either[CurrencyApiErrorResponse, CurrencyRatesResponse]] = {
        Future.successful(Right(
          CurrencyRatesResponse(
            base = base,
            timestamp = ZonedDateTime.of(2018, 11, 6, 15, 15, 15, 0, ZoneId.of("UTC")),
            rates = Map(
              Currency("EUR") -> BigDecimal(currencyRate)
            )
          )
        ))
      }
    }

    val monitoringConfig = CurrencyMonitoringConfig.load(ConfigFactory.load())

    val onChangeHook = mock[CurrencyRatesChangeHook]

    val monitoringStreamSpawn = new CurrencyMonitoringStreamSpawn(rateService, monitoringConfig, onChangeHook)

    val baseCurrency = Currency("USD")

    def withRunningCurrencyMonitoringStream[T](mockitoAssertion: => T): Unit = {
      when(onChangeHook.execute(baseCurrency)).thenReturn(Future.successful(Done))

      val killSwitch = monitoringStreamSpawn.spawn(baseCurrency)

      Thread.sleep(3000L)

      mockitoAssertion

      killSwitch.shutdown()
    }
  }

}
