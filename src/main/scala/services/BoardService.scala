package services

import model.BoardId
import model.Word
import model.Board
import java.time.Instant
import model.BoardNotFound
import model.BoardError
import infra.BoardRepository
import model

trait BoardService:
  def addWord(id: BoardId, word: Word): zio.IO[BoardError, Board]
  def create(question: String, ttl: Long): zio.IO[Throwable, Board]
  def get(id: BoardId): zio.IO[BoardError, Board]

final class BoardServiceImpl(repo: BoardRepository) extends BoardService:
  def create(question: String, ttl: Long): zio.IO[Throwable, Board] =
    val board: Board = Board.create(question, ttl)
    repo.save(board).as(board)

  def get(id: BoardId): zio.IO[BoardError, Board] = repo.get(id)

  def addWord(id: BoardId, word: Word): zio.IO[BoardError, Board] =
    val stageBoard = repo.get(id)
