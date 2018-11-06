package pl.nadoba.currencyapi.fixer

import java.time.LocalDate

import pl.nadoba.currencyapi.models.Currency

import scala.concurrent.Future

trait FixerClient {
  def getLatestCurrencyRates(base: Currency, targetOpt: Option[Currency]): Future[FixerResponse]
  def getHistoricalCurrencyRates(base: Currency, localDate: LocalDate, targetOpt: Option[Currency]): Future[FixerResponse]
}
