package pl.nadoba.currencyapi.service

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import pl.nadoba.currencyapi.config.CurrencyMonitoringConfig
import pl.nadoba.currencyapi.models.Currency

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait CurrencyRatesChangeHook {
  def execute(currency: Currency): Future[Done]
}

class CurrencyRatesChangeHookImpl(monitoringConfig: CurrencyMonitoringConfig)
  (implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends CurrencyRatesChangeHook with PlayJsonSupport {

  import pl.nadoba.currencyapi.formats.JsonFormats.currencyWrites

  private val webhookUri = Uri(monitoringConfig.webhook)

  override def execute(currency: Currency): Future[Done] = {

    val result = for {
      requestEntity <- Marshal(currency).to[RequestEntity]
      postRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = webhookUri,
        entity = requestEntity
      )
      response <- Http().singleRequest(postRequest)
      _ = println(s"Webhook called for currency ${currency.symbol} - response status ${response.status}")
      _ = response.entity.discardBytes()
    } yield Done

    result.recover {
      case ex =>
        println(s"Error when calling webhook - $ex")
        Done
    }
  }
}
