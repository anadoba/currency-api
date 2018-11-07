package pl.nadoba.currencyapi

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.{Assertion, BeforeAndAfterEach, MustMatchers}
import pl.nadoba.currencyapi.config.FixerConfig
import pl.nadoba.currencyapi.fixer.{FixerClientImpl, FixerErrorInfo, FixerErrorResponse, FixerRatesResponse}
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.testUtils.{ActorSpec, ScalaFuturesConfigured}
import play.api.libs.json.Json

class FixerClientSpec
  extends ActorSpec("FixerClientSpec")
    with BeforeAndAfterEach
    with ScalaFuturesConfigured
    with MustMatchers {

  val accessKey = "123456"
  val host = "localhost"
  val port = 5432
  val baseUrl = s"http://$host:$port/api/"
  val fixerConfig = FixerConfig(accessKey, baseUrl)
  val fixerClient = new FixerClientImpl(fixerConfig)

  val wireMockServer = new WireMockServer(port)

  override def beforeEach: Unit = {
    super.beforeEach()
    wireMockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterEach: Unit = {
    wireMockServer.stop()
    super.afterEach()
  }

  "Fixer Client" should {

    "execute latest currency rates request without target currency specified to Fixer API" in new Context {
      stub(
        url = s"/api/latest?access_key=$accessKey&base=$base",
        jsonPart =
          """
            |"rates":{
            |    "EUR":0.874195,
            |    "AUD":1.38095,
            |    "CAD":1.313385,
            |    "PLN":3.76427,
            |    "MXN":19.757303
            |  }
          """.stripMargin
      )

      val fixerResponse = fixerClient.getLatestCurrencyRates(baseCurrency, None).futureValue

      fixerResponse match {
        case f: FixerRatesResponse =>
          positiveResultAssertion(f, currencyRates)

        case _ => fail("error response received in positive scenario")
      }
    }

    "execute latest currency rates request with target currency specified to Fixer API" in new Context {
      stub(
        url = s"/api/latest?access_key=$accessKey&base=$base&symbols=$target",
        jsonPart =
          """
            |"rates":{
            |    "CAD":1.31172
            |  }
          """.stripMargin
      )

      val fixerResponse = fixerClient.getLatestCurrencyRates(baseCurrency, Some(targetCurrency)).futureValue

      fixerResponse match {
        case f: FixerRatesResponse =>
          positiveResultAssertion(f, cadRates)

        case _ => fail("error response received in positive scenario")
      }
    }

    "execute historical currency rates request without target currency specified to Fixer API" in new Context {
      stub(
        url = s"/api/$localDate?access_key=$accessKey&base=$base",
        jsonPart =
          """
            |"historical": true,
            |"rates":{
            |    "EUR":0.874195,
            |    "AUD":1.38095,
            |    "CAD":1.313385,
            |    "PLN":3.76427,
            |    "MXN":19.757303
            |  }
          """.stripMargin
      )

      val fixerResponse = fixerClient.getHistoricalCurrencyRates(baseCurrency, localDate, None).futureValue

      fixerResponse match {
        case f: FixerRatesResponse =>
          positiveResultAssertion(f, currencyRates)

        case _ => fail("error response received in positive scenario")
      }
    }

    "execute historical currency rates request with target currency specified to Fixer API" in new Context {
      stub(
        url = s"/api/$localDate?access_key=$accessKey&base=$base&symbols=$target",
        jsonPart =
          """
            |"historical": true,
            |"rates":{
            |    "CAD":1.31172
            |  }
          """.stripMargin
      )

      val fixerResponse = fixerClient.getHistoricalCurrencyRates(baseCurrency, localDate, Some(targetCurrency)).futureValue

      fixerResponse match {
        case f: FixerRatesResponse =>
          positiveResultAssertion(f, cadRates)

        case _ => fail("error response received in positive scenario")
      }
    }

    "return well-defined error on error response from Fixer platform" in new Context {
      stubFor(
        get(urlEqualTo(s"/api/latest?access_key=$accessKey&base=$base&symbols=$target"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                Json.parse(
                  """
                    |{
                    |  "success":false,
                    |  "error":{
                    |    "code":105,
                    |    "type":"base_currency_access_restricted"
                    |  }
                    |}
                  """.stripMargin
                ).toString
              )
          )
      )

      val fixerResponse = fixerClient.getLatestCurrencyRates(baseCurrency, Some(targetCurrency)).futureValue

      fixerResponse match {
        case f: FixerErrorResponse =>
          f.success mustEqual false
          f.error mustEqual FixerErrorInfo(105, "base_currency_access_restricted")

        case _ => fail("positive response received in negative scenario")
      }
    }
  }

  trait Context {

    val base = "USD"
    val baseCurrency = Currency(base)
    val target = "CAD"
    val targetCurrency = Currency("CAD")

    val timestamp = 1541548799L
    val localDate = LocalDate.of(2018, 11, 6)

    val cadRates = Map(
      targetCurrency -> 1.31172
    )

    val currencyRates = Map(
      Currency("EUR") -> 0.874195,
      Currency("AUD") -> 1.38095,
      Currency("CAD") -> 1.313385,
      Currency("PLN") -> 3.76427,
      Currency("MXN") -> 19.757303
    )

    def stub(url: String, jsonPart: String): Unit = {
      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(
                Json.parse(
                  """
                    |{
                    |  "success":true,
                    |  "timestamp":1541548799,
                    |  "historical":true,
                    |  "base":"USD",
                    |  "date":"2018-11-06",
                  """.stripMargin
                    + jsonPart + "}"
                ).toString
              )
          )
      )
    }

    def positiveResultAssertion(f: FixerRatesResponse, expectedRates: Map[Currency, Double]): Assertion = {
      f.success mustEqual true
      f.base mustEqual baseCurrency
      f.timestamp mustEqual timestamp
      f.date mustEqual localDate
      f.rates mustEqual expectedRates
    }
  }

}
