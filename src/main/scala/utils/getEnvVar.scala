package utils

import io.github.cdimascio.dotenv.Dotenv

case class RedisConfigError(message: String)
case class RedisAuthPack(host: String, port: String, password: String)

object getEnvVar:
  private val env = Dotenv.load()
  private def getVar(key: String): Either[RedisConfigError, String] =
    Option(env.get(key))
      .filter(_.nonEmpty)
      .toRight(RedisConfigError(s"$key environment variable is not set"))

  def getRedisPassword: Either[RedisConfigError, String] =
    getVar("REDIS_PASSWORD")
  def getRedisHost: Either[RedisConfigError, String] =
    getVar("REDIS_HOST")
  def getRedisPort: Either[RedisConfigError, String] =
    getVar("REDIS_PORT")

  def getRedisCreds: Either[RedisConfigError, RedisAuthPack] =
    for
      password <- getRedisPassword
      host <- getRedisHost
      port <- getRedisPort
    yield RedisAuthPack(host, port, password)

  def checkRedisCreds: Either[RedisConfigError, Unit] =
    for _ <- getRedisCreds
    yield println("Redis credentials are valid")
