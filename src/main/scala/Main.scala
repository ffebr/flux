import zio.ZIOAppDefault
import zio.http.Server
import api.boardRouter
import zio.ZLayer

object Main extends ZIOAppDefault:
  val serverConfigLayer: ZLayer[Any, Nothing, Server.Config] =
    ZLayer.succeed(Server.Config.default.port(5000))

  override val run = Server
    .serve(boardRouter.api)
    .provide(
      serverConfigLayer,
      Server.live
    )
