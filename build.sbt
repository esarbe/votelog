import sbt.Keys.libraryDependencies

val core =
  (project in file("core"))
    .settings(
      Settings.compiler,
      Settings.common,
      Dependencies.cats,
    )

val webapp =
  (project in file("webapp"))
    .settings(
      Settings.common,
      Settings.compiler,
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies ++=
        Seq(
          "org.scala-js" %%% "scalajs-dom" % "0.9.7",
          "com.lihaoyi" %% "scalatags" % "0.7.0",
          "com.lihaoyi" %% "scalarx" % "0.4.0"
        )
      )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(core)

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
    .dependsOn(core)
    .aggregate(webapp)
