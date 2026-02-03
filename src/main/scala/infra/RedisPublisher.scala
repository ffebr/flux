package infra

import model.{BoardEvent, WordAdded}
import zio.{IO, ZIO, ZLayer}
import zio.json.*
import zio.redis.Redis
import model.value
import zio.json.JsonCodec

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
