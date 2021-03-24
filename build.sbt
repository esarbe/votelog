import sbt.Keys.{libraryDependencies, parallelExecution}
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType
import sbt.IntegrationTest

val httpsVersion = "0.21.6"
val circeVersion = "0.13.0"
val doobieVersion = "0.9.0"
val catsVersion = "2.4.2"
val scalatestVersion = "3.1.2"
val scalacheckVersion = "1.14.3"


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


inThisBuild(List(
  scalacOptions ++= Seq(
    //"-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:postfixOps",
  ),
    scalaVersion := "2.13.5",
    organization := "org.esarbe.votelog",
    version := "0.0.1-SNAPSHOT",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for"  % "0.3.1"),
))


val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(
      libraryDependencies ++=
        Seq(
          "com.github.alexarchambault" %%% "scalacheck-shapeless_1.14" % "1.2.5" % Test,
          "org.typelevel" %%% "cats-core" % catsVersion withSources(),
          "io.circe" %%% "circe-core" % circeVersion,
          "io.circe" %%% "circe-generic" % circeVersion,
          "io.circe" %%% "circe-generic-extras"  % circeVersion,
          "io.circe" %%% "circe-parser" % circeVersion,
          "org.scalatest" %%% "scalatest" % scalatestVersion % Test,
          "org.scalacheck" %%% "scalacheck" % "1.15.3" % Test,
        )
    )
    .jsSettings(
      libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.6"
    )

val coreJvm = core.jvm
val coreJs = core.js

val webclient =
  (project in file("webclient"))
    .settings(
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("votelog.client.web.Application"),
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-core" % catsVersion,
          "org.scala-js" %%% "scalajs-dom" % "1.0.0",
          "in.nvilla" %%% "monadic-html" % "0.4.1+1-482ad6d6+20210310-1002-SNAPSHOT",
          "in.nvilla" %%% "monadic-rx" % "0.4.1+1-482ad6d6+20210310-1002-SNAPSHOT",
          "in.nvilla" %%% "monadic-rx-cats" % "0.4.1+1-482ad6d6+20210310-1002-SNAPSHOT",
          "org.scalatest" %% "scalatest" % "3.2.0" % Test,
          "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
        )
    )
    .dependsOn(core.js)
    .enablePlugins(ScalaJSPlugin)

val webserver =
  (project in file("webserver"))
    .configs(IntegrationTest)
    .settings(
      name := "webserver",
      mainClass in Compile := Some("votelog.app.Webserver"),
      IntegrationTest / parallelExecution := false,
      Defaults.itSettings,
      cats,
      common,
      circe,
      crypto,
      doobie,
      logging,
      web,
      test,
      mariaDb,
      decline,
    )
    .enablePlugins(JavaAppPackaging)
    .dependsOn(coreJvm)


val root =
  (project in file("."))
    .aggregate(webserver, webclient)
