package infra

import model.BoardEvent
import model.WordAdded
import model.value
import zio.IO
import zio.ZIO
import zio.ZLayer
import zio.json.*
import zio.json.JsonCodec
import zio.redis.Redis
import model.BoardEventPublishError
import model.BoardEventErrors
import java.util.UUID
import model.BoardCreated

trait Publisher:
  def publish(event: BoardEvent): IO[BoardEventErrors, Unit]

final class RedisPublisher(redis: Redis) extends Publisher:
  private val streamName = "board-events"
  override def publish(event: BoardEvent): IO[BoardEventErrors, Unit] =
    val jsonPayload =
      event.toJson // Serializes as BoardEvent, includes wrapper correctly
    for
      _ <- ZIO.logInfo(s"Publishing event: $jsonPayload")
      _ <- redis
        .xAdd(
          key = streamName,
          id = "*": String,
          noMakeStream = false
        )(
          "payload" -> jsonPayload
        )
        .returning[String]
        .tapError(e => ZIO.logError(s"REDIS CRASH: ${e.toString()}"))
        .mapError(e => BoardEventPublishError(e.getMessage()))
    yield ()

object RedisPublisher:
  val layer: ZLayer[Redis, Nothing, Publisher] =
    ZLayer {
      for (redisClient <- ZIO.service[Redis])
        yield RedisPublisher(redisClient)
    }
