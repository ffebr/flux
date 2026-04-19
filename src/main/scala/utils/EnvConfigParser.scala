package utils

import io.github.cdimascio.dotenv.Dotenv
import layers.RedisConfigError

case class EnvConfigError(message: String)

object EnvConfigParser:
  val env = Dotenv.configure().ignoreIfMissing().load()
  def getVar(key: String): Either[EnvConfigError, String] =
    Option(env.get(key))
      .filter(_.nonEmpty)
      .toRight(EnvConfigError(s"$key environment variable is not set"))
