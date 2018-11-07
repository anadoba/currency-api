package pl.nadoba.currencyapi.service

import akka.stream.SharedKillSwitch
import pl.nadoba.currencyapi.models.MonitoringServiceResponse.{AlreadyMonitoring, MonitoringServiceResponse, NotMonitoredYet, StartedMonitoring, StoppedMonitoring}
import pl.nadoba.currencyapi.models._

trait CurrencyMonitoringService {
  def listMonitoredCurrencies: MonitoringServiceResponse
  def startMonitoring(currency: Currency): MonitoringServiceResponse
  def stopMonitoring(currency: Currency): MonitoringServiceResponse
}

class CurrencyMonitoringServiceImpl(monitoringStreamSpawn: CurrencyMonitoringStreamSpawn) extends CurrencyMonitoringService {

  private var monitoredCurrencies: Map[Currency, SharedKillSwitch] = Map.empty

  override def listMonitoredCurrencies: MonitoringServiceResponse =
    MonitoringServiceResponse(monitoredCurrencies.keys.toSet, None)

  override def startMonitoring(currency: Currency): MonitoringServiceResponse = {
    val status = monitoredCurrencies.get(currency) match {
      case None =>
        val killSwitch = monitoringStreamSpawn.spawn(currency)
        monitoredCurrencies += (currency -> killSwitch)
        StartedMonitoring

      case Some(_) =>
        AlreadyMonitoring
    }

    MonitoringServiceResponse(monitoredCurrencies.keys.toSet, Some(status))
  }

  override def stopMonitoring(currency: Currency): MonitoringServiceResponse = {
    val status = monitoredCurrencies.get(currency) match {
      case Some(killSwitch) =>
        killSwitch.shutdown()
        monitoredCurrencies -= currency
        StoppedMonitoring

      case None =>
        NotMonitoredYet
    }

    MonitoringServiceResponse(monitoredCurrencies.keys.toSet, Some(status))
  }

}
