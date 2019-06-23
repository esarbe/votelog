import sbt.{addCompilerPlugin, _}
import sbt.Keys._

object Settings {

  val httpsVersion = "0.21.0-M1"
  val circeVersion = "0.12.0-M3"
  val doobieVersion = "0.8.0-M1"

  val common =
    Seq(
      scalaVersion := "2.13.0",
//      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for"  % "0.3.0"),
    )
}
