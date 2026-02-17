package services

import infra.BoardRepository
import model.Board
import model.BoardError
import model.BoardId
import model.BoardNotFound
import model.Word
import zio.Clock
import zio.ZIO
import zio.ZLayer

import java.time.Instant

trait BoardService:
  def addWord(id: BoardId, word: Word): zio.IO[BoardError, Board]
  def create(question: String, ttl: Long): zio.IO[BoardError, Board]
  def get(id: BoardId): zio.IO[BoardError, Board]

final class BoardServiceImpl(repo: BoardRepository) extends BoardService:
  def create(question: String, ttl: Long): zio.IO[BoardError, Board] =
    for
      now <- Clock.instant
      board = Board.create(question, ttl, now)
      _ <- repo.save(board)
    yield board

  def get(id: BoardId): zio.IO[BoardError, Board] = repo.get(id)

  def addWord(id: BoardId, word: Word): zio.IO[BoardError, Board] =
    for
      board <- repo.get(id)
      tuple <- ZIO.fromEither(board.addWord(word))
      (updatedBoard, _) = tuple
      _ <- repo.save(updatedBoard)
    yield updatedBoard

object BoardServiceImpl:
  val layer: ZLayer[BoardRepository, Nothing, BoardService] =
    ZLayer {
      for repo <- ZIO.service[BoardRepository]
      yield BoardServiceImpl(repo)
    }
