package model

import zio.json.JsonCodec

sealed trait BoardEvent derives JsonCodec

case class WordAdded(boardId: BoardId, word: Word, count: WordCount)
    extends BoardEvent derives JsonCodec
