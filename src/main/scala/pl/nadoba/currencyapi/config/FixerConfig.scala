package pl.nadoba.currencyapi.config

import com.typesafe.config.Config

case class FixerConfig(accessKey: String, baseUrl: String)

object FixerConfig {

  def load(config: Config): FixerConfig = {
    val fixerCfg = config.getConfig("fixer")

    FixerConfig(
      accessKey = fixerCfg.getString("access-key"),
      baseUrl = fixerCfg.getString("base-url")
    )
  }

}
