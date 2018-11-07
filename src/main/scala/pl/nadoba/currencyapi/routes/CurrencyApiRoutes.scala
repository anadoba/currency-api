package pl.nadoba.currencyapi.routes

import java.time.ZonedDateTime

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.service.{CurrencyMonitoringService, CurrencyRatesService}
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class CurrencyApiRoutes(currencyRatesService: CurrencyRatesService, monitoringService: CurrencyMonitoringService)(implicit ec: ExecutionContext) extends PlayJsonSupport {

  import pl.nadoba.currencyapi.models.JsonFormats.{currencyApiResponseWrites, currencyApiErrorResponseWrites}

  private val zonedDateTimeU = Unmarshaller.strict[String, ZonedDateTime](ZonedDateTime.parse(_))

  val currencyRatesRoute =
    path("rates") {
      parameters('base, 'target.?, 'timestamp.as(zonedDateTimeU).?) { (base, targetOpt, timestampOpt) =>
        get {
          val baseCurrency = Currency(base)
          val targetCurrencyOpt = targetOpt.map(Currency)

          val currencyRatesF = currencyRatesService.getCurrencyRates(baseCurrency, timestampOpt, targetCurrencyOpt)

          complete {
            currencyRatesF.map {
              case Right(ratesResponse) =>
                OK -> Json.toJson(ratesResponse)
              case Left(errorResponse) =>
                InternalServerError -> Json.toJson(errorResponse)
            }
          }
        }
      }
    }

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

  val route = currencyRatesRoute ~ monitoringRoute

}
