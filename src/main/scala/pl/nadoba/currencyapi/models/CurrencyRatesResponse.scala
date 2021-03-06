package pl.nadoba.currencyapi.models

import java.time.ZonedDateTime

case class CurrencyRatesResponse(
  base: Currency,
  timestamp: ZonedDateTime,
  rates: Map[Currency, BigDecimal]
)

case class CurrencyApiErrorResponse(
  message: String
)
