import api.BoardRouter
import infra.RedisBoardRepository
import layers.AppConfigLayer
import layers.RedisConfigLayer
import layers.RedisConsumerLayer
import services.BoardServiceImpl
import zio.ZIOAppDefault
import zio.http.Server
import infra.RedisPublisher
import infra.RedisConsumer
import zio.ZIO
import infra.BoardEventBus

object Main extends ZIOAppDefault:
  override val run =
    (ZIO.service[RedisConsumer] *> Server
      .serve(BoardRouter.api ++ BoardRouter.sseApi))
      .provide(
        AppConfigLayer.layer,
        RedisPublisher.layer,
        BoardServiceImpl.layer,
        RedisConfigLayer.layer,
        RedisBoardRepository.layer,
        BoardEventBus.layer,
        RedisConsumerLayer.layer
      )
