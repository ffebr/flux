val scala3Version = "3.8.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := ".",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies += "dev.zio" %% "zio-http" % "3.7.4",
    libraryDependencies += "dev.zio" %% "zio-redis" % "1.1.10",
    libraryDependencies += "dev.zio" %% "zio-json" % "0.7.44",
    libraryDependencies += "io.circe" %% "circe-yaml" % "0.16.0",
    libraryDependencies += "io.github.cdimascio" % "dotenv-java" % "3.2.0"
  )
