import sbt._
import sbt.Keys._

object Dependencies {

  import Settings.httpsVersion

  lazy val common =
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.monovore" %% "decline" % "0.5.0",
      "ch.qos.logback" % "logback-classic" % "0.9.24"
    )

  lazy val logging =
    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % "1.6.1",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.slf4j" % "slf4j-simple" % "1.7.5"
    )

  lazy val doobie =
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "0.6.0",
      "org.tpolecat" %% "doobie-h2" % "0.6.0",
      "org.tpolecat" %% "doobie-postgres"  % "0.6.0",
    )

  lazy val cats =
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.4.0" withSources(),
      "org.typelevel" %% "cats-effect" % "0.10.1" withSources(),
    )

  val web =
    libraryDependencies ++= Seq(
      "org.http4s"%% "http4s-blaze-server" % httpsVersion,
      "org.http4s" %% "http4s-circe" % httpsVersion,
      "org.http4s" %% "http4s-dsl" % httpsVersion
    )
}
