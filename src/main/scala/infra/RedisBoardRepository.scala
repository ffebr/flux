package infra

import model.{Board, BoardId}
import zio.*
import zio.json.*
import zio.json.DecoderOps
import zio.redis.Redis
import model.value
import java.time.Instant

final class RedisBoardRepository(redis: Redis) extends BoardRepository:
  private val keyPrefix = "board:"
  private def keyFor(id: BoardId): String = s"$keyPrefix${id.value}"

  def get(id: BoardId): IO[Throwable, Option[Board]] =
    for
      maybeString <- redis.get(keyFor(id)).returning[String]
      maybeBoard <- ZIO.foreach(maybeString) { boardJson =>
        ZIO
          .fromEither(boardJson.fromJson[Board])
          .mapError(err => new RuntimeException(s"Ошибка десериализации: $err"))
      }
    yield maybeBoard

  def save(board: Board): IO[Throwable, Unit] =
    for {
      now <- Clock.instant
      duration = java.time.Duration.between(now, board.expiresAt)
      ttl =
        if (!duration.isNegative && !duration.isZero) {
          Some(zio.Duration.fromJava(duration))
        } else {
          None
        }

      _ <- redis.set(
        key = keyFor(board.id),
        value = board.toJson,
        expireTime = ttl
      )
    } yield ()

object RedisBoardRepository:
  val layer: ZLayer[Redis, Nothing, BoardRepository] =
    ZLayer {
      for (redisClient <- ZIO.service[Redis])
        yield RedisBoardRepository(redisClient)
    }
