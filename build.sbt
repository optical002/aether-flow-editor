ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"


lazy val javafxVersion = "17.0.2"

lazy val root = (project in file("."))
  .settings(
    name := "aether-flow-editor",
    resolvers ++= Seq(
      "jitpack" at "https://jitpack.io",
      "Eclipse Releases" at "https://repo.eclipse.org/content/groups/efxclipse/"
    ),
    libraryDependencies ++= Seq(
      // Engine
      "com.github.optical002" % "aether-flow" % "0.0.5",

      // ScalaFX
//      "org.scalafx" %% "scalafx" % "24.0.0-R35",

      // Embedding gpu rendering into JavaFX
      "org.eclipse.fx" % "org.eclipse.fx.drift" % "1.0.0",

      // OpenJFX
      "org.openjfx" % "javafx-base" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-controls" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-graphics" % javafxVersion classifier "win",
      "org.openjfx" % "javafx-fxml" % javafxVersion classifier "win",
    ),
  )
