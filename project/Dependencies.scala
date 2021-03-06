import sbt._
import sbt.Keys._

object Dependencies {

  import Settings.{circeVersion, httpsVersion, doobieVersion, scalatestVersion, scalacheckVersion}

  lazy val common =
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum" % "1.6.1",
      "com.github.pureconfig" %% "pureconfig" % "0.13.0",
    )
  
  lazy val circe =
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % "0.13.0",
      "io.circe" %% "circe-parser" % circeVersion,
    )

  lazy val logging =
    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % "1.8.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.slf4j" % "slf4j-simple" % "1.7.30"
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
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test
    )

  lazy val decline =
    libraryDependencies += "com.monovore" %% "decline" % "1.2.0"

  lazy val postgres =
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.2"

  lazy val cats =
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.1.1" withSources(),
      "org.typelevel" %% "cats-effect" % "2.1.3" withSources(),
    )

  val web =
    libraryDependencies ++= Seq(
      "org.http4s"%% "http4s-blaze-server" % httpsVersion,
      "org.http4s" %% "http4s-circe" % httpsVersion,
      "org.http4s" %% "http4s-dsl" % httpsVersion
    )

  val test =
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % "test, it",
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test, it",
      "org.scalatestplus" %% "scalacheck-1-14" % "3.1.2.0" % "test, it",
    )

  val mariaDb = libraryDependencies += "org.mariadb.jdbc" % "mariadb-java-client" % "2.6.1"

}
