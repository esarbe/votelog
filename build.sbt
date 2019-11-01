import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType

val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(
      Settings.compiler,
      Settings.common,
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-core" % "2.0.0" withSources(),
        )
    )

val coreJvm = core.jvm
val coreJs = core.js

val webclient =
  (project in file("webclient"))
    .settings(
      Settings.common,
      Settings.compiler,
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-core" % "2.0.0" withSources(),
          "org.typelevel" %%% "cats-effect" % "2.0.0" withSources(),
          "org.scala-js" %%% "scalajs-dom" % "0.9.7",
          "in.nvilla" %%% "monadic-html" % "0.4.0-RC1",
          "in.nvilla" %%% "monadic-rx-cats" % "0.4.0-RC1"
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


