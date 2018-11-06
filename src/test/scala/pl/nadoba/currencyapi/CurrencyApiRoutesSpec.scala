package pl.nadoba.currencyapi

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{MustMatchers, WordSpec}
import pl.nadoba.currencyapi.routes.CurrencyApiRoutes

class CurrencyApiRoutesSpec extends WordSpec with ScalatestRouteTest with MustMatchers {

  "Currency Api" should {

    "respond with fixed string when calling example endpoint" in {
      Get() ~> CurrencyApiRoutes.route ~> check {
        responseAs[String] mustEqual "Hello Currency-Api assignment!"
      }
    }

  }

}
