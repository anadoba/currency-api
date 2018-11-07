package pl.nadoba.currencyapi.routes

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{MustMatchers, WordSpec}
import pl.nadoba.currencyapi.models.MonitoringServiceResponse.{MonitoringServiceResponse, StartedMonitoring, StoppedMonitoring}
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.service.CurrencyMonitoringService
import play.api.libs.json.Json

class CurrencyMonitoringRouteSpec extends WordSpec with MustMatchers with ScalatestRouteTest {

  "Currency Monitoring Route" should {

    "handle /monitoring request" in new Context {
      Get("/monitoring") ~> testRoute ~> check {
        Json.parse(responseAs[String]) mustEqual Json.parse(
          """
            |{
            |  "monitoredCurrencies" : [ "EUR", "USD" ]
            |}
          """.stripMargin
        )
      }
    }

    "handle /monitoring/start?currency=CAD" in new Context {
      Get("/monitoring/start?currency=CAD") ~> testRoute ~> check {
        Json.parse(responseAs[String]) mustEqual Json.parse(
          """
            |{
            |  "monitoredCurrencies" : [ "EUR", "USD", "CAD" ],
            |  "status": "started-monitoring"
            |}
          """.stripMargin
        )
      }
    }

    "handle /monitoring/stop?currency=USD" in new Context {
      Get("/monitoring/stop?currency=USD") ~> testRoute ~> check {
        Json.parse(responseAs[String]) mustEqual Json.parse(
          """
            |{
            |  "monitoredCurrencies" : [ "EUR" ],
            |  "status": "stopped-monitoring"
            |}
          """.stripMargin
        )
      }
    }

  }

  trait Context {

    val monitoringService = new CurrencyMonitoringService {
      override def listMonitoredCurrencies: MonitoringServiceResponse =
        MonitoringServiceResponse(Set(Currency("EUR"), Currency("USD")))

      override def startMonitoring(currency: Currency): MonitoringServiceResponse =
        MonitoringServiceResponse(Set(Currency("EUR"), Currency("USD"), Currency("CAD")), Some(StartedMonitoring))

      override def stopMonitoring(currency: Currency): MonitoringServiceResponse =
        MonitoringServiceResponse(Set(Currency("EUR")), Some(StoppedMonitoring))
    }

    val testRoute = new CurrencyMonitoringRoute(monitoringService).monitoringRoute
  }
}
