package model

import utils.uuidShortCode
import zio.json._

import java.time.Instant

opaque type BoardId = String
object BoardId:
  def apply(s: String): BoardId = s
  def generate(): BoardId = uuidShortCode.gen()
  given JsonCodec[BoardId] = JsonCodec[String].transform(BoardId.apply, _.value)
extension (boardId: BoardId) def value: String = boardId

case class Board(
    id: BoardId,
    title: String,
    question: String,
    expiresAt: Instant,
    words: Map[Word, WordCount]
) derives JsonCodec:
  def addWord(
      word: Word,
      now: Instant = Instant.now()
  ): Either[BoardError, (Board, Word)] =
    if now.isAfter(expiresAt) then Left(BoardExpiredError)
    else
      val normalizedWord = word.normalize()
      val newWords = words.get(normalizedWord) match
        case Some(count) => words.updated(normalizedWord, count.increment())
        case None        => words.updated(normalizedWord, WordCount(1))
      Right((this.copy(words = newWords), normalizedWord))

object Board:
  def apply(
      title: String,
      question: String,
      expiresAt: Instant
  ): Board =
    Board(
      id = BoardId.generate(),
      title = title,
      question = question,
      expiresAt = expiresAt,
      words = Map.empty[Word, WordCount]
    )
