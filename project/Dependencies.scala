import sbt._

object Dependencies {

  object Version {
    val scala = "2.12.7"
  }

  val dependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )
}
