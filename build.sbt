import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "elevator_system",
    idePackagePrefix := Some("org.example"),
    assembly / mainClass := Some("org.example.Main"),
    assembly / assemblyJarName := "elevator_system.jar"
  )

libraryDependencies ++= dependencies