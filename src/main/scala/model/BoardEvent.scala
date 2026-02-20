package model

import zio.json.JsonCodec

sealed trait BoardEvent derives JsonCodec:
  val boardId: BoardId
  def eventType: String = this match
    case _: WordAdded => "word-added"

case class WordAdded(boardId: BoardId, word: Word) extends BoardEvent
    derives JsonCodec
