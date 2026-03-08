package infra

import zio.Ref
import model.Board
import model.BoardId
import model.BoardEvent
import model.BoardEventConsumptionError
import zio.redis.*
import zio.Hub
import model.BoardEventErrors
import zio.*
import zio.IO
import zio.json.*
import zio.ZIO
import scala.concurrent.duration._
import scala.jdk.DurationConverters._
import java.util.UUID
import zio.ZLayer
import model.WordAdded
import model.BoardsHubs
import model.BoardCreated

final class RedisConsumer(
    redis: AsyncRedis,
    hubs: BoardEventBus,
    repo: BoardRepository
):
  private val streamName = "board-events"
  private val groupName = "board-group"
  private val consumerId = UUID.randomUUID().toString

  private def ensureGroup() =
    redis
      .xGroupCreate(
        key = streamName,
        group = groupName,
        id = "$",
        mkStream = true
      )

  private def applyEvent(event: BoardEvent) =
    event match
      case e: WordAdded =>
        for
          board <- repo.get(e.boardId)
          (updatedBoard, _) <- ZIO.fromEither(board.addWord(e.word))
          _ <- repo.save(updatedBoard)
        yield ()
      case e: BoardCreated =>
        hubs.createHub(e.boardId).unit

  private def processMsg(msg: StreamEntry[String, String, String]) =
    (for
      payload <- ZIO
        .fromOption(msg.fields.get("payload"))
        .orElseFail(BoardEventConsumptionError("Missing payload field"))
      event <- ZIO
        .fromEither(payload.fromJson[BoardEvent])
        .mapError(err => BoardEventConsumptionError(s"Failed to decode: $err"))
      _ <- applyEvent(event)
      _ <- hubs.publish(event)
      _ <- ZIO.logInfo(event.toString())
      _ <- redis.xAck(streamName, groupName, msg.id)
    yield ()).catchAll { err =>
      ZIO.logError(s"Failed processing ${msg.id}: $err") *>
        redis.xAck(streamName, groupName, msg.id)
    }

  private def consumeBatch() =
    for
      rawResult <- redis
        .xReadGroup(
          group = groupName,
          consumer = consumerId,
          count = Some(10),
          block = Some(5000.millis),
          noAck = false
        )(streamName -> ">")
        .returning[String, String]
      streamChunks <- rawResult
      _ <- ZIO.foreachDiscard(streamChunks) {
        case StreamChunk(stream, messages) =>
          ZIO.foreachDiscard(messages)(processMsg)
      }
    yield ()

  def consumeForever() =
    ensureGroup() *>
      (for {
        _ <- consumeBatch()
      } yield ())
        .catchAll(err =>
          ZIO.logError(s"Error in consumer loop: $err") *> ZIO.sleep(1.second)
        )
        .forever

object RedisConsumer:
  def layer =
    ZLayer.scoped {
      for
        redis <- ZIO.service[AsyncRedis]
        repo <- ZIO.service[BoardRepository]
        hubs <- ZIO.service[BoardEventBus]
        consumer = new RedisConsumer(redis, hubs, repo)
        _ <- consumer
          .consumeForever()
          .catchAllCause(c => ZIO.logErrorCause("Consumer died!", c))
          .forkScoped
        _ <- ZIO.logInfo("RedisConsumer Fiber started!")
      yield consumer
    }
