package pl.nadoba.currencyapi.testUtils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class ActorSpec(actorSystemName: String)
  extends TestKit(ActorSystem(actorSystemName))
    with WordSpecLike
    with BeforeAndAfterAll {

  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  lazy val testConfig: Config = ConfigFactory.load()

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
