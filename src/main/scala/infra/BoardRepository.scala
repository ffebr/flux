package infra

import model.Board
import model.BoardError
import model.BoardId
import zio.*

trait BoardRepository:
  def get(id: BoardId): IO[BoardError, Board]
  def save(board: Board): IO[BoardError, Unit]
