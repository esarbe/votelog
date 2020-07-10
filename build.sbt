import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType
import Settings.circeVersion

val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(
      Settings.compiler,
      Settings.common,
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-laws" % "2.1.1" % Test,
          "com.github.alexarchambault" %%% "scalacheck-shapeless_1.14" % "1.2.5" % Test,
          "org.typelevel" %%% "cats-core" % Settings.catsVersion withSources(),
          "io.circe" %%% "circe-core" % circeVersion,
          "io.circe" %%% "circe-generic" % circeVersion,
          "io.circe" %%% "circe-parser" % circeVersion,
          "io.circe" %%% "circe-generic-extras"  % circeVersion,
          "org.julienrf" %%% "endpoints-algebra" % "0.12.0" exclude("org.scala-lang.modules", "scala-xml_2.13"),
          "org.julienrf" %%% "endpoints-json-schema-generic" % "0.13.0"
            exclude("org.scala-lang.modules", "scala-xml_2.12"),
          "org.scalatest" %%% "scalatest" % "3.2.0" % Test,
          "org.scalacheck" %%% "scalacheck" % "1.14.0" % Test,
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
      Settings.common,
      Settings.compiler,
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("votelog.client.web.Application"),
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-core" % Settings.catsVersion,
          "org.scala-js" %%% "scalajs-dom" % "1.0.0",
          "in.nvilla" %%% "monadic-html" % "0.4.0",
          "in.nvilla" %%% "monadic-rx-cats" % "0.4.0",
          "org.scalatest" %% "scalatest" % "3.2.0" % Test,
          "org.scalacheck" %% "scalacheck" % "1.14.3" % Test,
        )
    )
    .dependsOn(core.js)
    .enablePlugins(ScalaJSPlugin)

val webserver =
  (project in file("webserver"))
    .settings(
      name := "webserver",
      mainClass in Compile := Some("votelog.app.Webserver"),
      Settings.common,
      Settings.compiler,
      Dependencies.cats,
      Dependencies.common,
      Dependencies.circe,
      Dependencies.crypto,
      Dependencies.doobie,
      Dependencies.logging,
      Dependencies.web,
      Dependencies.test,
      Dependencies.mariaDb,
      Dependencies.decline,
    )
    .enablePlugins(JavaAppPackaging)
    .dependsOn(coreJvm)


val root =
  (project in file("."))
    .settings(
      Settings.compiler,
      ThisBuild / useSuperShell := false
    )
    .aggregate(webserver, webclient)
