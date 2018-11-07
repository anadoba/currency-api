package pl.nadoba.currencyapi.routes

import java.time.{ZoneId, ZonedDateTime}

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import pl.nadoba.currencyapi.models.{Currency, CurrencyRatesResponse}
import pl.nadoba.currencyapi.service.CurrencyRatesService
import play.api.libs.json.Json

import scala.concurrent.Future

class CurrencyRatesRouteSpec extends WordSpec with MustMatchers with ScalatestRouteTest with MockitoSugar {

  import pl.nadoba.currencyapi.formats.JsonFormats.currencyApiResponseWrites

  "Currency Rates Route" should {

    "handle localhost:9000/rates?base=USD" in new Context {
      when(currencyRatesService.getCurrencyRates(baseCurrency, None, None)).thenReturn(mockResponse)

      Get("/rates?base=USD") ~> testRoute ~> check {
        verify(currencyRatesService, times(1)).getCurrencyRates(baseCurrency, None, None)

        Json.parse(responseAs[String]) mustEqual expectedJson
      }
    }

    "handle localhost:9000/rates?base=USD&target=CAD" in new Context {
      when(currencyRatesService.getCurrencyRates(baseCurrency, None, Some(targetCurrency))).thenReturn(mockResponse)

      Get("/rates?base=USD&target=CAD") ~> testRoute ~> check {
        verify(currencyRatesService, times(1)).getCurrencyRates(baseCurrency, None, Some(targetCurrency))

        Json.parse(responseAs[String]) mustEqual expectedJson
      }
    }

    "handle localhost:9000/rates?base=USD&timestamp=2016-05-01T14:34:46Z" in new Context {
      when(currencyRatesService.getCurrencyRates(baseCurrency, Some(timestamp), None)).thenReturn(mockResponse)

      Get("/rates?base=USD&timestamp=2016-05-01T14:34:46Z") ~> testRoute ~> check {
        verify(currencyRatesService, times(1)).getCurrencyRates(baseCurrency, Some(timestamp), None)

        Json.parse(responseAs[String]) mustEqual expectedJson
      }
    }

    "handle localhost:9000/rates?base=USD&target=CAD&timestamp=2016-05-01T14:34:46Z" in new Context {
      when(currencyRatesService.getCurrencyRates(baseCurrency, Some(timestamp), Some(targetCurrency))).thenReturn(mockResponse)

      Get("/rates?base=USD&target=CAD&timestamp=2016-05-01T14:34:46Z") ~> testRoute ~> check {
        verify(currencyRatesService, times(1)).getCurrencyRates(baseCurrency, Some(timestamp), Some(targetCurrency))

        Json.parse(responseAs[String]) mustEqual expectedJson
      }
    }

  }

  trait Context {

    val base = "USD"
    val baseCurrency = Currency(base)

    val target = "CAD"
    val targetCurrency = Currency(target)

    val timestamp = ZonedDateTime.parse("2016-05-01T14:34:46Z")

    val exampleResponse = CurrencyRatesResponse(
      base = baseCurrency,
      timestamp = ZonedDateTime.of(2018, 11, 6, 15, 15, 15, 0, ZoneId.of("UTC")),
      rates = Map(Currency("EUR") -> BigDecimal(1234.3))
    )

    val mockResponse = Future.successful(Right(exampleResponse))

    val expectedJson = Json.toJson(exampleResponse)

    val currencyRatesService = mock[CurrencyRatesService]

    val testRoute = new CurrencyRatesRoute(currencyRatesService).currencyRatesRoute
  }

}
