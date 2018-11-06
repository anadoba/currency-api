package pl.nadoba.currencyapi

import pl.nadoba.currencyapi.models.Currency

object TestHelpers {

  implicit def stringDoubleTupleToCurrencyRate(tuple: (String, Double)): (Currency, BigDecimal) =
    (Currency(tuple._1), BigDecimal(tuple._2))
}
