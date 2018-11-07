package pl.nadoba.currencyapi.models

import play.api.libs.json.{JsString, JsValue, Json, Writes}

object MonitoringServiceResponse {

  case class MonitoringServiceResponse(
    monitoredCurrencies: Set[Currency],
    status: Option[MonitoringServiceResponseStatus] = None
  )

  sealed trait MonitoringServiceResponseStatus {
    def status: String
  }

  case object StartedMonitoring extends MonitoringServiceResponseStatus {
    override val status: String = "started-monitoring"
  }

  case object StoppedMonitoring extends MonitoringServiceResponseStatus {
    override val status: String = "stopped-monitoring"
  }

  case object AlreadyMonitoring extends MonitoringServiceResponseStatus {
    override val status: String = "already-monitoring"
  }

  case object NotMonitoredYet extends MonitoringServiceResponseStatus {
    override val status: String = "not-monitored-yet"
  }


  implicit val monitoringServiceResponseWrites: Writes[MonitoringServiceResponse] = new Writes[MonitoringServiceResponse] {
    override def writes(o: MonitoringServiceResponse): JsValue = {
      val baseJson = Json.obj(
        "monitoredCurrencies" -> o.monitoredCurrencies.map(c => JsString(c.symbol))
      )
      o.status.fold(baseJson) { status =>
        baseJson ++ Json.obj("status" -> JsString(status.status))
      }
    }

  }
}

