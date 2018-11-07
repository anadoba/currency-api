package pl.nadoba.currencyapi

import java.time.LocalDate

import org.scalatest.{MustMatchers, WordSpec}
import pl.nadoba.currencyapi.fixer.{FixerErrorInfo, FixerErrorResponse, FixerRatesResponse}
import pl.nadoba.currencyapi.models.{Currency, JsonFormats}
import play.api.libs.json.Json
import pl.nadoba.currencyapi.testUtils.TestHelpers.stringDoubleTupleToCurrencyRate

class JsonFormatsSpec extends WordSpec with MustMatchers {

  "Json Formats" should {
    "read successful JSON response from Fixer platform" in {
      val json = Json.parse(
        """
          |{
          |  "success":true,
          |  "timestamp":1541515745,
          |  "base":"GBP",
          |  "date":"2018-11-06",
          |  "rates":{
          |    "USD":1.307275,
          |    "AUD":1.807367,
          |    "CAD":1.715472,
          |    "PLN":4.934362,
          |    "MXN":25.936652
          |  }
          |}
        """.stripMargin)

      val fixerResponseRead = JsonFormats.fixerResponseReads.reads(json)
      fixerResponseRead.isSuccess mustEqual true

      fixerResponseRead.get mustEqual
        FixerRatesResponse(
          timestamp = 1541515745L,
          base = Currency("GBP"),
          date = LocalDate.of(2018, 11, 6),
          rates = Map("USD" -> 1.307275, "AUD" -> 1.807367, "CAD" -> 1.715472, "PLN" -> 4.934362, "MXN" -> 25.936652))
    }

    "read error JSON response from Fixer platform" in {
      val json = Json.parse(
        """
          |{
          |  "success":false,
          |  "error":{
          |    "code":105,
          |    "type":"base_currency_access_restricted"
          |  }
          |}
        """.stripMargin)

      val fixerResponseRead = JsonFormats.fixerResponseReads.reads(json)
      fixerResponseRead.isSuccess mustEqual true

      fixerResponseRead.get mustEqual FixerErrorResponse(
        error = FixerErrorInfo(code = 105, `type` = "base_currency_access_restricted")
      )
    }
  }

}
