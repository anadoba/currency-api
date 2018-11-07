package pl.nadoba.currencyapi.service

import akka.Done
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, urlEqualTo}
import org.scalatest.{BeforeAndAfterEach, MustMatchers}
import pl.nadoba.currencyapi.config.CurrencyMonitoringConfig
import pl.nadoba.currencyapi.models.Currency
import pl.nadoba.currencyapi.testUtils.{ActorSpec, ScalaFuturesConfigured}

import scala.concurrent.duration._
import com.github.tomakehurst.wiremock.client.WireMock._

class CurrencyRatesChangeHookSpec
  extends ActorSpec("CurrencyRatesChangeHookSpec")
  with BeforeAndAfterEach
  with ScalaFuturesConfigured
  with MustMatchers {

  val host = "localhost"
  val port = 7091
  val webhookUrl = s"http://$host:$port/webhooks"

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

  val monitoringConfig = CurrencyMonitoringConfig(webhookUrl, 1.minute)

  val currencyRatesChangeHook = new CurrencyRatesChangeHookImpl(monitoringConfig)

  "Currency Rates Change Hook" should {

    "call the webhook" in {

      stubFor(
        post(urlEqualTo("/webhooks"))
          .willReturn(
            aResponse()
              .withStatus(200)
          )
      )

      val currency = Currency("USD")

      val result = currencyRatesChangeHook.execute(currency).futureValue

      result mustEqual Done
    }

  }

}
