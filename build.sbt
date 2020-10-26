lazy val commonSettings = Seq(
  organization := "edu.berkeley.cs",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.12",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  ),
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.4.0",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8",
)

lazy val main = (project in file(".")).
  settings(name := "chisel-formal").
  settings(commonSettings: _*).
  settings(
    Test / scalacOptions += "-Xsource:2.11"
  )


