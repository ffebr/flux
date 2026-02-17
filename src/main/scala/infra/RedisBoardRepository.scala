package infra

import model.Board
import model.BoardError
import model.BoardId
import model.BoardNotFound
import model.StorageError
import model.value
import zio.*
import zio.json.*
import zio.json.DecoderOps
import zio.redis.Redis

import java.time.Duration

final class RedisBoardRepository(redis: Redis) extends BoardRepository:
  private val keyPrefix = "board:"
  private def keyFor(id: BoardId): String = s"$keyPrefix${id.value}"

  def get(id: BoardId): IO[BoardError, Board] =
    for
      maybeString <- redis
        .get(keyFor(id))
        .returning[String]
        .mapError(err => StorageError(err))
      boardString <- ZIO
        .fromOption(maybeString)
        .orElseFail(BoardNotFound(id))
      board <- ZIO
        .fromEither(boardString.fromJson[Board])
        .mapError(err => StorageError(err))
    yield board

  def save(board: Board): IO[BoardError, Unit] =
    for {
      now <- Clock.instant
      duration = Duration.between(now, board.expiresAt)
      ttl =
        if (!duration.isNegative && !duration.isZero) then
          Some(zio.Duration.fromJava(duration))
        else None

      _ <- redis
        .set(
          key = keyFor(board.id),
          value = board.toJson,
          expireTime = ttl
        )
        .mapError(err => StorageError(err))
    } yield ()

object RedisBoardRepository:
  val layer: ZLayer[Redis, Nothing, BoardRepository] =
    ZLayer {
      for (redisClient <- ZIO.service[Redis])
        yield RedisBoardRepository(redisClient)
    }
