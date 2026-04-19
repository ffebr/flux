package infra

import model.BoardEvent
import model.BoardId
import model.BoardsHubs
import model.BoardHub
import zio.*
import zio.stream.ZStream
import model.BoardEventErrors
import model.BoardEventSubscribeError

sealed trait BoardEventBus:
  def getHub(id: BoardId): IO[BoardEventErrors, BoardHub]
  def createHub(id: BoardId): UIO[BoardHub]
  def subscribe(id: BoardId): ZStream[Any, BoardEventErrors, BoardEvent]
  def publish(event: BoardEvent): UIO[Unit]

class BoardEventBusImpl(hubs: BoardsHubs, repo: BoardRepository)
    extends BoardEventBus:
  def getHub(id: BoardId): IO[BoardEventErrors, BoardHub] =
    for
      map <- hubs.get
      hub <- map.get(id) match
        case Some(h) => ZIO.succeed(h)
        case None    =>
          repo
            .get(id)
            .foldZIO(
              _ => ZIO.fail(BoardEventSubscribeError(s"Board $id not found")),
              _ => createHub(id)
            )
    yield hub

  def subscribe(id: BoardId): ZStream[Any, BoardEventErrors, BoardEvent] =
    ZStream
      .fromZIO(getHub(id))
      .flatMap(ZStream.fromHub(_))

  def publish(event: BoardEvent): UIO[Unit] =
    getHub(event.boardId).flatMap(hub => hub.publish(event)).ignore

  def createHub(id: BoardId): UIO[BoardHub] =
    for
      newHub <- Hub.unbounded[BoardEvent]
      hub <- hubs.modify { map =>
        map.get(id) match
          case Some(existingHub) => (existingHub, map)
          case None              => (newHub, map + (id -> newHub))
      }
    yield hub

object BoardEventBus:
  val layer: ZLayer[BoardRepository, Nothing, BoardEventBus] = ZLayer.fromZIO(
    for
      repo <- ZIO.service[BoardRepository]
      hubs <- Ref.make(Map.empty[BoardId, BoardHub])
    yield BoardEventBusImpl(hubs, repo)
  )
