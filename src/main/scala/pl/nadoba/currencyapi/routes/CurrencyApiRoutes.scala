package pl.nadoba.currencyapi.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._

object CurrencyApiRoutes {

  val route =
    pathEndOrSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "Hello Currency-Api assignment!"))
      }
    }

}
