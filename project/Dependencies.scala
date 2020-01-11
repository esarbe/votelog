import sbt._
import sbt.Keys._

object Dependencies {

  import Settings.{circeVersion, httpsVersion, doobieVersion}

  lazy val common =
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum" % "1.5.13",
      "com.github.pureconfig" %% "pureconfig" % "0.11.1",
    )
  
  lazy val circe =
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % "0.12.2",
      "io.circe" %% "circe-parser" % circeVersion,
    )

  lazy val logging =
    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % "1.8.2",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )

  lazy val crypto =
    libraryDependencies ++= Seq(
      "org.reactormonk" %% "cryptobits" % "1.3"
    )

  lazy val doobie =
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    )

  lazy val decline =
    libraryDependencies += "com.monovore" %% "decline" % "1.0.0"

  lazy val postgres =
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.2"

  lazy val cats =
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.0.0" withSources(),
      "org.typelevel" %% "cats-effect" % "2.0.0" withSources(),
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
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    )

  val mariaDb = libraryDependencies += "org.mariadb.jdbc" % "mariadb-java-client" % "2.4.2"

}
