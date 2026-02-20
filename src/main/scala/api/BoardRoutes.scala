package api

import api.ErrorResponseHandler.handleErrors
import model.AddWordRequest
import model.BoardError
import model.BoardId
import model.BoardResponse
import model.CreateBoardRequest
import model.ErrorResponse
import model.SerializationError
import model.StorageError
import model.Word
import services.BoardService
import zio.*
import zio.http.*
import zio.json.*
import model.BoardEvent
import streams.BoardEventStream
import zio.stream.ZStream

object BoardRouter:
  def api: Routes[BoardService, Response] = Routes(
    Method.POST / "boards" -> handler { (req: Request) =>
      handleErrors {
        for
          boardService <- ZIO.service[BoardService]
          body <- req.body.asString
          data <- ZIO
            .fromEither(body.fromJson[CreateBoardRequest])
            .mapError(_ => SerializationError())
          board <- boardService.create(data.question, data.ttl)
        yield Response.json(BoardResponse.fromBoard(board).toJson)
      }
    },
    Method.PATCH / "boards" / string("id") / "words" -> handler {
      (id: String, req: Request) =>
        handleErrors {
          for
            boardService <- ZIO.service[BoardService]
            body <- req.body.asString
            data <- ZIO
              .fromEither(body.fromJson[AddWordRequest])
              .mapError(_ => SerializationError())
            board <- boardService.addWord(BoardId(id), Word(data.word))
          yield Response
            .json( /*BoardResponse.fromBoard(board)*/ board.toJson)
            .status(Status.Accepted)
        }
    },
    Method.GET / "boards" / string("id") -> handler {
      (id: String, req: Request) =>
        handleErrors {
          for
            boardService <- ZIO.service[BoardService]
            board <- boardService.get(BoardId(id))
          yield Response.json(BoardResponse.fromBoard(board).toJson)
        }
    }
  )

  def sseApi: Routes[Hub[BoardEvent], Nothing] = Routes(
    Method.GET / "boards" / string("id") / "events" -> handler {
      (id: String, req: Request) =>
        for
          hub <- ZIO.service[Hub[BoardEvent]]
          stream = BoardEventStream.eventStream(BoardId(id), hub)
          heartbeat = ZStream
            .repeat(ServerSentEvent.heartbeat)
            .schedule(Schedule.spaced(5.seconds))
          fullStream = stream.merge(heartbeat)
        yield Response.fromServerSentEvents(fullStream)
    }
  )
