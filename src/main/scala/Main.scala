import AppConfig.JsonCodecSupplier
import api.boardRouter
import infra.RedisBoardRepository
import services.BoardServiceImpl
import zio.*
import zio.ZIO
import zio.ZIOAppDefault
import zio.ZLayer
import zio.http.Server
import zio.redis.*
import zio.redis.CodecSupplier
import zio.redis.Redis
import zio.redis.RedisConfig
import zio.schema.Schema
import zio.schema.codec.BinaryCodec
import zio.schema.codec.JsonCodec
import utils.getEnvVar

object AppConfig:
  val redisPassword: String = sys.env.getOrElse("REDIS_PASSWORD", "2111")
  val redisConfig =
    RedisConfig(host = "151.245.139.61", port = 6379)

  val serverConfig = Server.Config.default.port(8080)

  object JsonCodecSupplier extends CodecSupplier:
    def get[A: Schema]: BinaryCodec[A] = JsonCodec.schemaBasedBinaryCodec

object Main extends ZIOAppDefault:
  val redisCredCheckLayer =
    ZIO.fromEither(getEnvVar.checkRedisCreds)
  val redisAuthLayer = ZLayer.fromZIO {
    ZIO.serviceWithZIO[Redis](_.auth(AppConfig.redisPassword))
  }

  override val run = redisCredCheckLayer *> Server
    .serve(boardRouter.api)
    .provide(
      ZLayer.succeed(AppConfig.serverConfig) >>> Server.live,
      BoardServiceImpl.layer,
      (
        ZLayer.succeed(AppConfig.redisConfig) ++
          ZLayer.succeed(AppConfig.JsonCodecSupplier)
      ) >>> Redis.singleNode,
      redisAuthLayer,
      RedisBoardRepository.layer
    )
