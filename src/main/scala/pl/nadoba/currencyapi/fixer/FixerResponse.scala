package pl.nadoba.currencyapi.fixer

import java.time.LocalDate

import pl.nadoba.currencyapi.models.Currency

sealed trait FixerResponse {
  def success: Boolean
}

case class FixerRatesResponse(
  override val success: Boolean = true,
  timestamp: Long,
  base: Currency,
  date: LocalDate,
  rates: Map[Currency, BigDecimal]
) extends FixerResponse

case class FixerErrorResponse(
  override val success: Boolean = false,
  error: FixerErrorInfo
) extends FixerResponse

case class FixerErrorInfo(code: Int, `type`: String)