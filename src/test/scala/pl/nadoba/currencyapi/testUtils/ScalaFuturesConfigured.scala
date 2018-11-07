package pl.nadoba.currencyapi.testUtils

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Span}

trait ScalaFuturesConfigured extends ScalaFutures {
  private val timeoutSpan = Span(1000L, Milliseconds)
  private val intervalSpan = Span(500L, Milliseconds)

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = timeoutSpan, interval = intervalSpan)
}
