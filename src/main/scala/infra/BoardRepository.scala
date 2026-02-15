package infra

import model.BoardId
import model.Board
import zio.*
import model.BoardError

trait BoardRepository:
  def get(id: BoardId): IO[BoardError, Board]
  def save(board: Board): IO[Throwable, Unit]
