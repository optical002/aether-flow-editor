ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"

lazy val root = (project in file("."))
  .settings(
    name := "aether-flow-editor",
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      // Scalafx
      "org.scalafx" %% "scalafx" % "24.0.0-R35",
      "com.github.optical002" % "aether-flow" % "0.0.5",
    ),
  )
