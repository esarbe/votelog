import sbt.Keys.libraryDependencies

val core =
  crossProject.in(file("core"))
    .settings(
      Settings.compiler,
      Settings.common,
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-core" % "2.0.0" withSources(),
          "org.typelevel" %%% "cats-effect" % "2.0.0" withSources(),
        )
    )

val coreJvm = core.jvm
val coreJs = core.js

val webapp =
  (project in file("webapp"))
    .settings(
      Settings.common,
      Settings.compiler,
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++=
        Seq(
          "org.typelevel" %%% "cats-core" % "2.0.0" withSources(),
          "org.typelevel" %%% "cats-effect" % "2.0.0" withSources(),
          "org.scala-js" %%% "scalajs-dom" % "0.9.7",
          "com.lihaoyi" %% "scalatags" % "0.7.0",
          "com.lihaoyi" %% "scalarx" % "0.4.0",
          "in.nvilla" %%% "monadic-html" % "0.4.0-RC1",
          "in.nvilla" %%% "monadic-rx-cats" % "0.4.0-RC1"
        )
    )
    .dependsOn(core.js)


val root =
  (project in file("."))
    .settings(
      name := "application",
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

