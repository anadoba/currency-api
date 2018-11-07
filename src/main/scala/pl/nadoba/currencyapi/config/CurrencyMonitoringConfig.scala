package pl.nadoba.currencyapi.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

case class CurrencyMonitoringConfig(webhook: String, interval: FiniteDuration)

object CurrencyMonitoringConfig {

  def load(config: Config): CurrencyMonitoringConfig = {
    val monitoringCfg = config.getConfig("currency-monitoring")

    CurrencyMonitoringConfig(
      webhook = monitoringCfg.getString("webhook"),
      interval = FiniteDuration(monitoringCfg.getDuration("interval").toMillis, TimeUnit.MILLISECONDS)
    )
  }
}