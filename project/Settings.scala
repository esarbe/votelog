import sbt.{addCompilerPlugin, _}
import sbt.Keys._

object Settings {

  val httpsVersion = "0.21.0-M5"
  val circeVersion = "0.13.0-M1"
  val doobieVersion = "0.8.6"

  val compiler =
    scalacOptions ++= Seq(
      //"-Xfatal-warnings",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:postfixOps",
      "-Ypartial-unification",
    )

  val common =
    Seq(
      scalaVersion := "2.12.10",
      organization := "org.esarbe.votelog",
      version := "0.0.1-SNAPSHOT",
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for"  % "0.3.0"),
    )
}
