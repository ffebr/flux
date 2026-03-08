package model

import zio.json.JsonCodec
import zio.Hub
import zio.Ref

type BoardHub = Hub[BoardEvent]
type BoardsHubs = Ref[Map[BoardId, Hub[BoardEvent]]]

sealed trait BoardEvent derives JsonCodec:
  val boardId: BoardId
  def eventType: String = this match
    case _: WordAdded    => "word-added"
    case _: BoardCreated => "board-created"

case class WordAdded(boardId: BoardId, word: Word) extends BoardEvent
    derives JsonCodec

case class BoardCreated(boardId: BoardId) extends BoardEvent derives JsonCodec
