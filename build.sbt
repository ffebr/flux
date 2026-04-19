val scala3Version = "3.8.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "flux",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies += "dev.zio" %% "zio-streams" % "2.1.24",
    libraryDependencies += "dev.zio" %% "zio-http" % "3.7.4",
    libraryDependencies += "dev.zio" %% "zio-redis" % "1.1.10",
    libraryDependencies += "dev.zio" %% "zio-json" % "0.7.44",
    libraryDependencies += "io.github.cdimascio" % "dotenv-java" % "3.2.0"
  )

import scala.sys.process._

val redisPort = settingKey[Int]("Port for Redis container")
val redisPassword = settingKey[String]("Password for Redis container")
val redisContainerName = "flux-redis-test"

val redisStart = taskKey[Unit]("Start Redis container for local tests")
val redisStop = taskKey[Unit]("Stop and remove Redis container")
val redisClean = taskKey[Unit]("Flush all data from Redis container")

redisPort := 6379
redisPassword := "2111"

redisStart := {
  val port = redisPort.value
  val password = redisPassword.value
  val name = redisContainerName

  val checkCmd = s"docker ps -a --filter name=$name --format {{.Names}}"
  val existing = checkCmd.!!.trim

  if (existing == name) {
    println(s"Container $name already exists. Starting it...")
    s"docker start $name".!
  } else {
    println(s"Creating and starting Redis container $name on port $port...")
    val runCmd =
      s"docker run --name $name -p $port:6379 -e REDIS_PASSWORD=$password -d redis redis-server --requirepass $password"
    runCmd.!
  }
}

redisStop := {
  val name = redisContainerName
  println(s"Stopping and removing Redis container $name and its volumes...")
  s"docker rm -f -v $name".!
}

redisClean := {
  val name = redisContainerName
  val password = redisPassword.value
  println(s"Cleaning Redis container $name...")
  s"docker exec -it $name redis-cli -a $password flushall".!
}
