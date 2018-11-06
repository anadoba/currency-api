package pl.nadoba.currencyapi.fixer

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import pl.nadoba.currencyapi.config.FixerConfig
import pl.nadoba.currencyapi.models.Currency

import scala.concurrent.{ExecutionContext, Future}

trait FixerClient {
  def getLatestCurrencyRates(base: Currency, targetOpt: Option[Currency]): Future[FixerResponse]
  def getHistoricalCurrencyRates(base: Currency, localDate: LocalDate, targetOpt: Option[Currency]): Future[FixerResponse]
}

class FixerClientImpl(fixerConfig: FixerConfig)
  (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext)
  extends FixerClient
    with PlayJsonSupport {

  import pl.nadoba.currencyapi.models.JsonFormats._

  private val tokenQueryParam = "access_key" -> fixerConfig.accessKey

  override def getLatestCurrencyRates(base: Currency, targetOpt: Option[Currency]): Future[FixerResponse] = {
    val request = prepareRequest(pathSuffix = "latest", base, targetOpt)

    executeRequest(request)
  }

  override def getHistoricalCurrencyRates(base: Currency, localDate: LocalDate, targetOpt: Option[Currency]): Future[FixerResponse] = {
    val request = prepareRequest(pathSuffix = localDate.toString, base, targetOpt)

    executeRequest(request)
  }

  private def prepareRequest(pathSuffix: String, base: Currency, targetOpt: Option[Currency]): HttpRequest = {
    val uri = prepareUri(pathSuffix, base, targetOpt)
    HttpRequest(uri = uri).withHeaders(RawHeader("Accept", "application/json"))
  }

  private def prepareUri(pathSuffix: String, base: Currency, targetOpt: Option[Currency]): Uri = {
    val uri = Uri(fixerConfig.baseUrl + pathSuffix)
    val baseParam = "base" -> base.symbol
    val targetParamOpt = targetOpt.map(target => "symbols" -> target.symbol)
    val queryParams = Seq(Some(tokenQueryParam), Some(baseParam), targetParamOpt).flatten
    val query = Query(queryParams:_*)
    uri.withQuery(query)
  }

  private def executeRequest(request: HttpRequest): Future[FixerResponse] = {
    Http()
      .singleRequest(request)
      .flatMap(Unmarshal(_).to[FixerResponse])
  }

}
