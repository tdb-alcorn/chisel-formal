lazy val commonSettings = Seq(
  organization := "edu.berkeley.cs",
  version := "0.1-SNAPSHOT",
  //scalaVersion := "2.13.3",
  scalaVersion := "2.12.10",
//  crossScalaVersions := Seq("2.12.10", "2.11.12"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  ),
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  libraryDependencies ++= Seq("chisel3","firrtl").map { dep: String =>
    "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
  },
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8",
)

scalacOptions ++= Seq(
  "-Xsource:2.11"
)

lazy val main = (project in file(".")).
  settings(name := "chisel-formal").
  settings(commonSettings: _*)
//  dependsOn(macros)

lazy val macros = (project in file("macros")).
  settings(name := "chisel3-formal-macros").
  settings(commonSettings: _*)
//  settings(publishSettings: _*)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
// The following are the default development versions, not the "release" versions.
val defaultVersions = Map(
  "chisel3" -> "3.4.0-RC1",
  "firrtl" -> "1.4.0-RC1"
)
