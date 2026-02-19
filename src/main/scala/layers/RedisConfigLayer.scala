package layers

import io.github.cdimascio.dotenv.Dotenv
import zio.*
import zio.redis.*
import zio.schema.Schema
import zio.schema.codec.BinaryCodec
import zio.schema.codec.JsonCodec
import utils.EnvConfigParser.getVar

case class RedisConfigError(message: String)
case class RedisAuthPack(host: String, port: String, password: String)

object RedisConfigLayer:
  object JsonCodecSupplier extends CodecSupplier:
    def get[A: Schema]: BinaryCodec[A] = JsonCodec.schemaBasedBinaryCodec

  def getRedisPassword: Either[RedisConfigError, String] =
    getVar("REDIS_PASSWORD").left.map(e => RedisConfigError(e.message))
  def getRedisHost: Either[RedisConfigError, String] =
    getVar("REDIS_HOST").left.map(e => RedisConfigError(e.message))
  def getRedisPort: Either[RedisConfigError, String] =
    getVar("REDIS_PORT").left.map(e => RedisConfigError(e.message))

  def getRedisCreds: IO[RedisConfigError, RedisAuthPack] =
    for
      password <- ZIO.fromEither(getRedisPassword)
      host <- ZIO.fromEither(getRedisHost)
      port <- ZIO.fromEither(getRedisPort)
    yield RedisAuthPack(host, port, password)

  val redisCredsLayer = ZLayer.fromZIO(getRedisCreds)

  val redisConfigLayer = ZLayer.fromZIO {
    for
      creds <- ZIO.service[RedisAuthPack]
      port <- ZIO
        .attempt(creds.port.toInt)
        .mapError(_ => RedisConfigError("Invalid redis port"))
    yield RedisConfig(creds.host, port)
  }

  val redisAuthLayer = ZLayer.fromZIO {
    for
      redis <- ZIO.service[Redis]
      creds <- ZIO.service[RedisAuthPack]
      _ <- redis.auth(creds.password)
    yield redis
  }

  val RedisLayer = (
    (redisCredsLayer >+> redisConfigLayer ++ ZLayer.succeed(
      JsonCodecSupplier
    )) >>> Redis.singleNode
      ++
      redisCredsLayer
  ) >>> redisAuthLayer
