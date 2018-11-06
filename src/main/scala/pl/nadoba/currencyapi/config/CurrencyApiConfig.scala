package pl.nadoba.currencyapi.config

import com.typesafe.config.Config

case class CurrencyApiConfig(host: String, port: Int)

object CurrencyApiConfig {

  def load(config: Config): CurrencyApiConfig = {
    val currencyApiCfg = config.getConfig("currency-api")

    CurrencyApiConfig(
      host = currencyApiCfg.getString("host"),
      port = currencyApiCfg.getInt("port")
    )
  }
}
