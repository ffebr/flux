package api

import model.BoardError
import model.BoardExpiredError
import model.BoardNotFound
import model.ErrorResponse
import model.SerializationError
import model.StorageError
import model.value
import zio.ZIO
import zio.http.Response
import zio.http.Status
import zio.json.EncoderOps
import model.BoardEventSourcingError
import model.BoardEventErrors

object ErrorResponseHandler:
  def fromBoardError(err: BoardError) =
    err match
      case BoardExpiredError =>
        (Status.Conflict, ErrorResponse("Board Board has expired"))
      case BoardNotFound(id) =>
        (Status.NotFound, ErrorResponse(s"Board ${id.value} not found"))
      case StorageError(message, cause) =>
        (
          Status.InternalServerError,
          ErrorResponse(message, cause.map(_.toString))
        )
      case BoardEventSourcingError(msg) =>
        (Status.InternalServerError, ErrorResponse(msg))

  def fromSerializationError(err: SerializationError) =
    (Status.UnprocessableEntity, ErrorResponse(err.message))

  def fromBoardEventErrors(err: BoardEventErrors) =
    (Status.NotFound, ErrorResponse(err.message))

  def handleErrors[R](
      effect: ZIO[
        R,
        BoardError | SerializationError | BoardEventErrors | Throwable,
        Response
      ]
  ) =
    effect.catchAll {
      case err: BoardError =>
        val (status, body) = fromBoardError(err)
        ZIO.succeed(
          Response.json(body.toJson).status(status)
        )
      case err: SerializationError =>
        val (status, body) = fromSerializationError(err)
        ZIO.succeed(
          Response
            .json(body.toJson)
            .status(status)
        )
      case err: BoardEventErrors =>
        val (status, body) = fromBoardEventErrors(err)
        ZIO.succeed(
          Response
            .json(body.toJson)
            .status(status)
        )
      case _ =>
        ZIO.succeed(
          Response.error(Status.InternalServerError, "spooky ooops")
        )
    }
