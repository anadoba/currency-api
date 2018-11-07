package pl.nadoba.currencyapi.service

import pl.nadoba.currencyapi.models.MonitoringServiceResponse.{AlreadyMonitoring, MonitoringServiceResponse, NotMonitoredYet, StartedMonitoring, StoppedMonitoring}
import pl.nadoba.currencyapi.models._

trait CurrencyMonitoringService {
  def listMonitoredCurrencies: MonitoringServiceResponse
  def startMonitoring(currency: Currency): MonitoringServiceResponse
  def stopMonitoring(currency: Currency): MonitoringServiceResponse
}

class CurrencyMonitoringServiceImpl(ratesService: CurrencyRatesService) extends CurrencyMonitoringService {

  private var monitoredCurrencies: Set[Currency] = Set.empty

  override def listMonitoredCurrencies: MonitoringServiceResponse =
    MonitoringServiceResponse(monitoredCurrencies, None)

  override def startMonitoring(currency: Currency): MonitoringServiceResponse = {
    val status = if (monitoredCurrencies.contains(currency)) {
      AlreadyMonitoring
    } else {
      monitoredCurrencies += currency
      StartedMonitoring
    }

    MonitoringServiceResponse(monitoredCurrencies, Some(status))
  }

  override def stopMonitoring(currency: Currency): MonitoringServiceResponse = {
    val status = if (monitoredCurrencies.contains(currency)) {
      monitoredCurrencies -= currency
      StoppedMonitoring
    } else {
      NotMonitoredYet
    }

    MonitoringServiceResponse(monitoredCurrencies, Some(status))
  }
}
