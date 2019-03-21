import sbt.{addCompilerPlugin, _}
import sbt.Keys._

object Settings {

  val httpsVersion = "0.18.21"

  val common =
    Seq(
      scalacOptions += "-Ypartial-unification",
      scalaVersion := "2.12.4",
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for"  % "0.2.4")
    )
}
