package pl.nadoba.currencyapi

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import pl.nadoba.currencyapi.routes.CurrencyApiRoutes

import scala.io.StdIn
import scala.util.Try

object Main extends App {

  implicit val system = ActorSystem("currency-api-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val host = "localhost"
  val port = 9000

  val bindingFuture = Http().bindAndHandle(CurrencyApiRoutes.route, host, port)

  println(s"Server online at http://$host:$port/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(shutdownHook)

  private def shutdownHook(httpTerminationTry: Try[Done]): Unit = {
    httpTerminationTry.map(_ => ()).recover {
      case ex => println(s"Error during shutdown: $ex")
    }
    system.terminate()
  }

}
