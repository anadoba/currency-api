package pl.nadoba.currencyapi.formats

import pl.nadoba.currencyapi.fixer.{FixerErrorInfo, FixerErrorResponse, FixerRatesResponse, FixerResponse}
import pl.nadoba.currencyapi.models.{Currency, CurrencyApiErrorResponse, CurrencyRatesResponse}
import play.api.libs.json._


object JsonFormats {

  implicit val currencyReads: Reads[Currency] = new Reads[Currency] {
    override def reads(json: JsValue): JsResult[Currency] = json match {
      case JsString(symbol) => JsSuccess(Currency(symbol))
      case unexpected => JsError(s"JSON string expected! Gor $unexpected")
    }
  }

  implicit val currencyWrites: Writes[Currency] = new Writes[Currency] {
    override def writes(o: Currency): JsValue = JsString(o.symbol)
  }

  implicit val fixerRatesResponseReads: Reads[FixerRatesResponse] = {

    implicit object CurrencyRateMapReads extends Reads[Map[Currency, BigDecimal]] {
      def reads(json: JsValue): JsResult[Map[Currency, BigDecimal]] = json match {
        case jsObject: JsObject =>
          val ratesMap = jsObject.fields.collect {
            case (currency, JsNumber(rate)) => (Currency(currency), rate)
          }.toMap

          JsSuccess(ratesMap)
        case unexpected => JsError(s"JSON object expected! Got $unexpected")
      }
    }

    Json.reads[FixerRatesResponse]
  }

  implicit val fixerErrorInfoReads: Reads[FixerErrorInfo] = Json.reads[FixerErrorInfo]

  implicit val fixerErrorResponseReads: Reads[FixerErrorResponse] = Json.reads[FixerErrorResponse]

  implicit val fixerResponseReads: Reads[FixerResponse] = new Reads[FixerResponse] {
    override def reads(json: JsValue): JsResult[FixerResponse] = {
      json match {
        case jsObject: JsObject =>
          val fieldsMap = jsObject.fields.toMap

          fieldsMap.get("success") match {
            case Some(JsTrue) => fixerRatesResponseReads.reads(json)
            case Some(JsFalse) => fixerErrorResponseReads.reads(json)
            case _ => JsError("Unexpected 'success' field value - boolean is expected")
          }

        case unexpected: JsValue =>
          JsError(s"JSON object expected! Got $unexpected")
      }
    }
  }

  implicit val currencyApiResponseWrites = {
    implicit object CurrencyRateMapWrites extends Writes[Map[Currency, BigDecimal]] {
      def writes(rateMap: Map[Currency, BigDecimal]): JsValue = {
        val fields = rateMap.map {
          case (currency, rate) => (currency.symbol, JsNumber(rate))
        }

        JsObject(fields)
      }
    }

    Json.writes[CurrencyRatesResponse]
  }

  implicit val currencyApiErrorResponseWrites = Json.writes[CurrencyApiErrorResponse]
}
