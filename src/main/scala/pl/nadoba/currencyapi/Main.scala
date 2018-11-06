package pl.nadoba.currencyapi

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import pl.nadoba.currencyapi.config.{CurrencyApiConfig, FixerConfig}
import pl.nadoba.currencyapi.routes.CurrencyApiRoutes

import scala.io.StdIn
import scala.util.Try

object Main extends App {

  implicit val system = ActorSystem("currency-api-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config = ConfigFactory.load()
  val fixerConfig = FixerConfig.load(config)
  val currencyApiConfig = CurrencyApiConfig.load(config)
  import currencyApiConfig.{host => currencyApiHost, port => currencyApiPort}

  val bindingFuture = Http().bindAndHandle(CurrencyApiRoutes.route, currencyApiHost, currencyApiPort)

  println(s"Server online at http://$currencyApiHost:$currencyApiPort/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(shutdownHook)

  private def shutdownHook(httpTerminationTry: Try[Done]): Unit = {
    httpTerminationTry.recover {
      case ex => println(s"Error during shutdown: $ex")
    }
    system.terminate()
  }

}
