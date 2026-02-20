package model

import zio.json.JsonCodec

sealed trait BoardError:
  val message: String

case object BoardExpiredError extends BoardError:
  val message: String = ""

case class BoardNotFound(id: BoardId) extends BoardError:
  val message: String = s"Board ${id.value} not found"

case class BoardEventSourcingError(message: String) extends BoardError

case class StorageError private (message: String, cause: Option[Throwable])
    extends BoardError

object StorageError:
  def apply(t: Throwable): StorageError =
    StorageError(t.getMessage, Some(t))

  def apply(msg: String): StorageError =
    StorageError(msg, None)
