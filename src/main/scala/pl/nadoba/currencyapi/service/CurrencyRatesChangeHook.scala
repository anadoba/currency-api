package pl.nadoba.currencyapi.service

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import pl.nadoba.currencyapi.config.CurrencyMonitoringConfig
import pl.nadoba.currencyapi.models.Currency

import scala.concurrent.{ExecutionContext, Future}

trait CurrencyRatesChangeHook {
  def execute(currency: Currency): Future[Done]
}

class CurrencyRatesChangeHookImpl(monitoringConfig: CurrencyMonitoringConfig)
  (implicit system: ActorSystem, ec: ExecutionContext) extends CurrencyRatesChangeHook with PlayJsonSupport {

  import pl.nadoba.currencyapi.models.JsonFormats.currencyWrites

  private val webhookUri = Uri(monitoringConfig.webhook)

  override def execute(currency: Currency): Future[Done] = {

    for {
      requestEntity <- Marshal(currency).to[RequestEntity]
      postRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = webhookUri,
        entity = requestEntity
      )
      _ <- Http().singleRequest(postRequest)
    } yield {
      println(s"Webhook called - currency ${currency.symbol}")
      Done
    }

  }
}
