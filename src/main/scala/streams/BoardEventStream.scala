package streams

import model.BoardEvent
import zio.Hub
import model.BoardId
import zio.stream.ZStream
import model.WordAdded
import zio.json.*
import zio.http.ServerSentEvent
import model.BoardEventErrors

object BoardEventStream:
  def eventStream(stream: ZStream[Any, BoardEventErrors, BoardEvent]) =
    stream.map { event =>
      ServerSentEvent(
        data = event.toJson,
        eventType = Some(event.eventType)
      )
    }
