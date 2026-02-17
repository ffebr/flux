package model

import zio.http.Response
import zio.http.Status
import zio.json.DeriveJsonEncoder
import zio.json.JsonCodec
import zio.json.JsonEncoder

import java.time.Instant

case class BoardResponse(
    id: String,
    question: String,
    expiresAt: Instant,
    words: Map[String, Int]
) derives JsonCodec

object BoardResponse:
  def fromBoard(board: Board): BoardResponse =
    BoardResponse(
      id = board.id.value,
      question = board.question,
      expiresAt = board.expiresAt,
      words = board.words.view.map { case (w, c) => w.value -> c.value }.toMap
    )

case class CreateBoardRequest(question: String, ttl: Long) derives JsonCodec
case class AddWordRequest(word: String) derives JsonCodec
