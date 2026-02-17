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

trait Publisher:
  def publish(event: BoardEvent): IO[Throwable, Unit]

final class RedisPublisher(redis: Redis) extends Publisher:
  override def publish(event: BoardEvent): IO[Throwable, Unit] =
    event match
      case wa: WordAdded =>
        val channel = s"board-events:${wa.boardId.value}"
        val jsonPayload = event.toJson
        redis.publish(channel, jsonPayload).unit.mapError(e => e)

object RedisPublisher:
  val layer: ZLayer[Redis, Nothing, Publisher] =
    ZLayer {
      for (redisClient <- ZIO.service[Redis])
        yield RedisPublisher(redisClient)
    }
