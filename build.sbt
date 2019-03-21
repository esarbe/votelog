name := "votelog"


val root =
  (project in file("."))
    .settings(
      organization := "org.esarbe",
      version := "0.0.1-SNAPSHOT",
      name := "votelog",
      mainClass in Compile := Some("votelog.app.Webserver"),
      Settings.common,
      Dependencies.cats,
      Dependencies.logging,
      Dependencies.common,
      Dependencies.web,
    )
    .enablePlugins(JavaAppPackaging)
