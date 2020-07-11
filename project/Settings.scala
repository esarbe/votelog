import sbt.{addCompilerPlugin, _}
import sbt.Keys._

object Settings {

  val httpsVersion = "1.0.0-M3"
  val circeVersion = "0.13.0"
  val doobieVersion = "0.9.0"
  val catsVersion = "2.1.1"
  val scalatestVersion = "3.1.2"
  val scalacheckVersion = "1.14.3"

  val compiler =
    scalacOptions ++= Seq(
      //"-Xfatal-warnings",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:postfixOps",
    )

  val common =
    Seq(
      scalaVersion := "2.13.3",
      organization := "org.esarbe.votelog",
      version := "0.0.1-SNAPSHOT",
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for"  % "0.3.1"),
    )
}
