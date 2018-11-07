package pl.nadoba.currencyapi.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._

object IndexRoute {

  val route = pathEndOrSingleSlash {
    get {
      complete {
        HttpEntity(
          contentType = ContentTypes.`text/html(UTF-8)`,
          string =
            """
              |<html>
              |<h1>Flow Currency Rates</h1>
              |<h2>View currency rates</h2>
              |<ul>
              |  <li><a href="http://localhost:9000/rates?base=USD">http://localhost:9000/rates?base=USD</a></li>
              |  <li><a href="http://localhost:9000/rates?base=USD&target=CAD">http://localhost:9000/rates?base=USD&target=CAD</a></li>
              |  <li><a href="http://localhost:9000/rates?base=USD&timestamp=2016-05-01T14:34:46Z">http://localhost:9000/rates?base=USD&timestamp=2016-05-01T14:34:46Z</a></li>
              |  <li><a href="http://localhost:9000/rates?base=USD&target=CAD&timestamp=2016-05-01T14:34:46Z">http://localhost:9000/rates?base=USD&target=CAD&timestamp=2016-05-01T14:34:46Z</a></li>
              |</ul>
              |<h2>Currency rates monitoring</h2>
              |<ul>
              |  <li><a href="http://localhost:9000/monitoring">http://localhost:9000/monitoring</a></li>
              |  <li><a href="http://localhost:9000/monitoring/start?currency=USD">http://localhost:9000/monitoring/start?currency=USD</a></li>
              |  <li><a href="http://localhost:9000/monitoring/stop?currency=USD">http://localhost:9000/monitoring/stop?currency=USD</a></li>
              |</ul>
              |</html>
            """.stripMargin
        )
      }
    }
  }

}
