package layers

import utils.EnvConfigParser.getVar
import scala.util.Try
import zio.ZLayer
import zio.http.Server
import zio.durationInt

case class AppConfigError(message: String)

object AppConfigLayer:
  def getAppPort = getVar("APP_PORT").left.map(e => AppConfigError(e.message))
  val port = for
    rawPort <- getAppPort
    port <- Try(rawPort.toInt).toEither.left.map(_ =>
      AppConfigError("Invalid port")
    )
  yield port

  val appConfig =
    Server.Config.default.port(port.getOrElse(8080)).idleTimeout(5.minutes)

  val layer = ZLayer.succeed(appConfig) >>> Server.live
