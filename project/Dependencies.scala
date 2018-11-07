import sbt._

object Dependencies {

  object Version {
    val scala = "2.12.7"
    val akkaHttp = "10.1.5"
    val akkaStream = "2.5.12"
    val playJson = "2.6.10"
  }

  val dependencies = Seq(
    "com.typesafe.akka" %% "akka-http" % Version.akkaHttp,
    "de.heikoseeberger" %% "akka-http-play-json" % "1.22.0",
    "com.typesafe.play" %% "play-json" % Version.playJson,
    "com.typesafe.akka" %% "akka-stream" % Version.akkaStream,
    "com.typesafe" % "config" % "1.3.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % "test",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "com.github.tomakehurst" % "wiremock" % "2.19.0" % "test",
    "org.mockito" % "mockito-core" % "2.8.47" % "test"
  )
}
