package pl.nadoba.currencyapi.routes

import java.time.ZonedDateTime

import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.service.CurrencyRatesService
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class CurrencyRatesRoute(currencyRatesService: CurrencyRatesService)(implicit ec: ExecutionContext) extends PlayJsonSupport {

  import pl.nadoba.currencyapi.formats.JsonFormats.{currencyApiErrorResponseWrites, currencyApiResponseWrites}

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

}
