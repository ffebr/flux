package infra

import model.BoardId
import model.Board
import zio.*

trait BoardRepository:
  def get(id: BoardId): IO[Throwable, Option[Board]]
  def save(board: Board): IO[Throwable, Unit]
