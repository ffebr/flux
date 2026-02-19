package layers

import utils.EnvConfigParser.getVar
import scala.util.Try
import zio.ZLayer
import zio.http.Server

case class AppConfigError(message: String)

object AppConfigLayer:
  def getAppPort = getVar("APP_PORT").left.map(e => AppConfigError(e.message))
  val port = for
    rawPort <- getAppPort
    port <- Try(rawPort.toInt).toEither.left.map(_ =>
      AppConfigError("Invalid port")
    )
  yield port

  val appConfig = Server.Config.default.port(port.getOrElse(8080))

  val AppConfigLayer = ZLayer.succeed(appConfig) >>> Server.live
