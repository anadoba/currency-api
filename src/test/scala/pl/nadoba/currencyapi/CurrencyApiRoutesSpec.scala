package pl.nadoba.currencyapi

import java.time.ZonedDateTime

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{MustMatchers, WordSpec}
import pl.nadoba.currencyapi.models.{Currency, CurrencyApiErrorResponse, CurrencyRatesResponse}
import pl.nadoba.currencyapi.routes.CurrencyApiRoutes
import pl.nadoba.currencyapi.service.CurrencyRatesService

import scala.concurrent.Future

class CurrencyApiRoutesSpec extends WordSpec with ScalatestRouteTest with MustMatchers {

  "Currency Api" should {

     "always pass" in {
       1 mustEqual 1 // TODO: real test scenario
     }

  }

  trait Context {
    val currencyRatesService = new CurrencyRatesService {
      override def getCurrencyRates(base: Currency, zonedDateTimeOpt: Option[ZonedDateTime], targetOpt: Option[Currency]): Future[Either[CurrencyApiErrorResponse, CurrencyRatesResponse]] =
        Future.successful(Left(CurrencyApiErrorResponse("fake")))
    }
    val testRoute = new CurrencyApiRoutes(currencyRatesService).route
  }

}
