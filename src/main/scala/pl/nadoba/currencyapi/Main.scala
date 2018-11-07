package pl.nadoba.currencyapi

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import pl.nadoba.currencyapi.config.{CurrencyApiConfig, CurrencyMonitoringConfig, FixerConfig}
import pl.nadoba.currencyapi.fixer.FixerClientImpl
import pl.nadoba.currencyapi.routes.{CurrencyMonitoringRoute, CurrencyRatesRoute}
import pl.nadoba.currencyapi.service.{CurrencyMonitoringServiceImpl, CurrencyMonitoringStreamSpawn, CurrencyRatesChangeHookImpl, CurrencyRatesServiceImpl}
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn
import scala.util.Try

object Main extends App {

  implicit val system = ActorSystem("currency-api-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val config = ConfigFactory.load()
  val fixerConfig = FixerConfig.load(config)
  val currencyApiConfig = CurrencyApiConfig.load(config)
  val monitoringConfig = CurrencyMonitoringConfig.load(config)
  import currencyApiConfig.{host => currencyApiHost, port => currencyApiPort}

  val fixerClient = new FixerClientImpl(fixerConfig)
  val currencyRatesService = new CurrencyRatesServiceImpl(fixerClient)

  val currencyRatesChangeHook = new CurrencyRatesChangeHookImpl(monitoringConfig)
  val monitoringStreamSpawn = new CurrencyMonitoringStreamSpawn(currencyRatesService, monitoringConfig, currencyRatesChangeHook)
  val monitoringService = new CurrencyMonitoringServiceImpl(monitoringStreamSpawn)
  val currencyRatesRoute = new CurrencyRatesRoute(currencyRatesService)
  val currencyMonitoringRoute = new CurrencyMonitoringRoute(monitoringService)
  val route = currencyRatesRoute.currencyRatesRoute ~ currencyMonitoringRoute.monitoringRoute

  val bindingFuture = Http().bindAndHandle(route, currencyApiHost, currencyApiPort)

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
    materializer.shutdown()
  }

}
