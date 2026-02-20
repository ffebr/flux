package model

import utils.uuidShortCode
import zio.json.*

import java.time.Instant
import java.util.UUID

opaque type BoardId = String
object BoardId:
  def apply(s: String): BoardId = s
  def generate(): BoardId = uuidShortCode.gen(uuid = UUID.randomUUID())
  given JsonCodec[BoardId] = JsonCodec[String].transform(BoardId.apply, _.value)
extension (boardId: BoardId) def value: String = boardId

case class Board(
    id: BoardId,
    question: String,
    expiresAt: Instant,
    words: Map[Word, WordCount]
) derives JsonCodec:
  def addWord(
      word: Word,
      now: Instant = Instant.now()
  ): Either[BoardError, (Board, Map[Word, WordCount])] =
    if now.isAfter(expiresAt) then Left(BoardExpiredError)
    else
      val normalizedWord = word.normalize()
      val newWords = words.get(normalizedWord) match
        case Some(count) => words.updated(normalizedWord, count.increment())
        case None        => words.updated(normalizedWord, WordCount(1))
      Right(
        (
          this.copy(words = newWords),
          newWords
        )
      )

object Board:
  def create(
      question: String,
      ttlSeconds: Long,
      now: Instant
  ): Board =
    Board(
      id = BoardId.generate(),
      question = question,
      expiresAt = now.plusSeconds(ttlSeconds),
      words = Map.empty[Word, WordCount]
    )
