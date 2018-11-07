package pl.nadoba.currencyapi.routes

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{complete, get, parameter, path}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.service.CurrencyMonitoringService
import play.api.libs.json.Json
import akka.http.scaladsl.server.Directives._

class CurrencyMonitoringRoute(monitoringService: CurrencyMonitoringService) extends PlayJsonSupport {

  val monitoringRoute =
    get {
      path("monitoring") {
        complete {
          val response = monitoringService.listMonitoredCurrencies
          OK -> Json.toJson(response)
        }
      } ~
        path("monitoring" / "start") {
          parameter("currency") { currencyString =>
            val currency = Currency(currencyString)
            complete {
              val response = monitoringService.startMonitoring(currency)
              OK -> Json.toJson(response)
            }
          }
        } ~
        path("monitoring" / "stop") {
          parameter("currency") { currencyString =>
            val currency = Currency(currencyString)
            complete {
              val response = monitoringService.stopMonitoring(currency)
              OK -> Json.toJson(response)
            }
          }
        }
    }

}
