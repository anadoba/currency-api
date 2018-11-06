import sbt._

object Dependencies {

  object Version {
    val scala = "2.12.7"
    val akkaHttp = "10.1.5"
    val akkaStream = "2.5.12"
  }

  val dependencies = Seq(
    "com.typesafe.akka" %% "akka-http" % Version.akkaHttp,
    "com.typesafe.akka" %% "akka-stream" % Version.akkaStream,
    "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % "test",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )
}
