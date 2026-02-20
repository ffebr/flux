package streams

import model.BoardEvent
import zio.Hub
import model.BoardId
import zio.stream.ZStream
import model.WordAdded
import zio.json.*
import zio.http.ServerSentEvent

object BoardEventStream:
  def eventStream(id: BoardId, hub: Hub[BoardEvent]) =
    ZStream
      .fromHub(hub)
      .filter(_.boardId == id)
      .map(event =>
        ServerSentEvent(
          data = event.toJson,
          eventType = Some(event.eventType)
        )
      )
