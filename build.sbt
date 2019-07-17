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
