package pl.nadoba.currencyapi.service

import java.time.ZonedDateTime

import pl.nadoba.currencyapi.fixer.{FixerClient, FixerErrorResponse, FixerRatesResponse}
import pl.nadoba.currencyapi.models.{Currency, CurrencyApiErrorResponse, CurrencyRatesResponse}

import scala.concurrent.{ExecutionContext, Future}

trait CurrencyRatesService {
  def getCurrencyRates(base: Currency, zonedDateTimeOpt: Option[ZonedDateTime], targetOpt: Option[Currency]): Future[Either[CurrencyApiErrorResponse, CurrencyRatesResponse]]
}

class CurrencyRatesServiceImpl(fixerClient: FixerClient)(implicit ec: ExecutionContext) extends CurrencyRatesService {

  def getCurrencyRates(base: Currency, zonedDateTimeOpt: Option[ZonedDateTime], targetOpt: Option[Currency]): Future[Either[CurrencyApiErrorResponse, CurrencyRatesResponse]] = {
    val fixerResponseF = zonedDateTimeOpt match {
      case Some(zonedDateTime) =>
        val date = zonedDateTime.toLocalDate
        fixerClient.getHistoricalCurrencyRates(base, date, targetOpt)
      case None =>
        fixerClient.getLatestCurrencyRates(base, targetOpt)
    }

    fixerResponseF.map {
      case ratesResponse: FixerRatesResponse =>
        val zonedDateTime = zonedDateTimeOpt.getOrElse(ZonedDateTime.now())
        val response = CurrencyRatesResponse(base, zonedDateTime, ratesResponse.rates)
        Right(response)

      case FixerErrorResponse(_, errorInfo) =>
        val errorMsg = s"Fixer platform returned error [${errorInfo.`type`}], code ${errorInfo.code}"
        Left(CurrencyApiErrorResponse(errorMsg))
    }.recover {
      case ex =>
        val errorMsg = s"Unexpected error when calling Fixer platform: ${ex.getMessage}"
        Left(CurrencyApiErrorResponse(errorMsg))
    }
  }

}
