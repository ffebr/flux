package services

import infra.BoardRepository
import model.Board
import model.BoardError
import model.BoardId
import model.BoardNotFound
import model.Word
import zio.Clock
import zio.ZIO
import zio.IO
import zio.ZLayer

import java.time.Instant
import infra.Publisher
import model.WordAdded
import model.BoardEventSourcingError

trait BoardService:
  def addWord(id: BoardId, word: Word): IO[BoardError, BoardId]
  def create(question: String, ttl: Long): IO[BoardError, Board]
  def get(id: BoardId): zio.IO[BoardError, Board]

final class BoardServiceImpl(repo: BoardRepository, publisher: Publisher)
    extends BoardService:
  def create(question: String, ttl: Long): zio.IO[BoardError, Board] =
    for
      now <- Clock.instant
      board = Board.create(question, ttl, now)
      _ <- repo.save(board)
    yield board

  def get(id: BoardId): IO[BoardError, Board] = repo.get(id)

  def addWord(id: BoardId, word: Word): IO[BoardError, BoardId] =
    for
      board <- repo.get(id)
      _ <- publisher
        .publish(WordAdded(id, word))
        .mapError(e => BoardEventSourcingError(e.message))
      _ <- ZIO.logInfo((id, word).toString())
    yield id

object BoardServiceImpl:
  val layer: ZLayer[BoardRepository & Publisher, Nothing, BoardService] =
    ZLayer {
      for
        repo <- ZIO.service[BoardRepository]
        publisher <- ZIO.service[Publisher]
      yield BoardServiceImpl(repo, publisher)
    }
