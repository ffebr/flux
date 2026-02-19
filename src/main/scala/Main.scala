import api.boardRouter
import infra.RedisBoardRepository
import layers.AppConfigLayer.AppConfigLayer
import layers.RedisConfigLayer.RedisLayer
import services.BoardServiceImpl
import zio.ZIOAppDefault
import zio.http.Server

object Main extends ZIOAppDefault:
  override val run = Server
    .serve(boardRouter.api)
    .provide(
      AppConfigLayer,
      BoardServiceImpl.layer,
      RedisLayer,
      RedisBoardRepository.layer
    )
