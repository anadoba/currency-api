package pl.nadoba.currencyapi.routes

import java.time.ZonedDateTime

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.service.CurrencyRatesService
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class CurrencyApiRoutes(currencyRatesService: CurrencyRatesService)(implicit ec: ExecutionContext) extends  PlayJsonSupport {

  import pl.nadoba.currencyapi.models.JsonFormats.{currencyApiResponseWrites, currencyApiErrorResponseWrites}

  private val zonedDateTimeU = Unmarshaller.strict[String, ZonedDateTime](ZonedDateTime.parse(_))

  val route =
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

}
