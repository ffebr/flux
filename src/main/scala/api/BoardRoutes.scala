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
import infra.BoardEventBus
import model.BoardEventErrors

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

  def sseApi: Routes[BoardEventBus, Response] = Routes(
    Method.GET / "boards" / string("id") / "events" -> handler {
      (id: String, req: Request) =>
        handleErrors {
          for
            bus <- ZIO.service[BoardEventBus]
            boardId = BoardId(id)
            _ <- bus.getHub(boardId)
            stream = BoardEventStream
              .eventStream(bus.subscribe(boardId))
            heartbeat = ZStream.succeed(ServerSentEvent.heartbeat) ++
              ZStream.tick(5.seconds).map(_ => ServerSentEvent.heartbeat)
            fullStream = stream.merge(heartbeat).catchAll(_ => ZStream.empty)
          yield Response.fromServerSentEvents(fullStream)
        }
    }
  )
