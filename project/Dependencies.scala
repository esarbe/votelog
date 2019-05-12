import sbt._
import sbt.Keys._

object Dependencies {

  import Settings.{circeVersion, httpsVersion, doobieVersion}

  lazy val common =
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum" % "1.5.12",
      "com.monovore" %% "decline" % "0.5.0",
      "com.github.pureconfig" %% "pureconfig" % "0.10.2",
      "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.10.2",
    )
  
  lazy val circe =
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)

  lazy val logging =
    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % "1.7.0",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )

  lazy val crypto =
    libraryDependencies ++= Seq(
      "org.reactormonk" %% "cryptobits" % "1.2"
    )

  lazy val doobie =
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    )

  lazy val cats =
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.6.0" withSources(),
      "org.typelevel" %% "cats-effect" % "1.2.0" withSources(),
    )

  val web =
    libraryDependencies ++= Seq(
      "org.http4s"%% "http4s-blaze-server" % httpsVersion,
      "org.http4s" %% "http4s-circe" % httpsVersion,
      "org.http4s" %% "http4s-dsl" % httpsVersion
    )

  val test =
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
    )


}
