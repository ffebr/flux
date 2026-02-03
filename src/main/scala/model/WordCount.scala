package model

import zio.json.JsonCodec

case class WordCount(value: Int) derives JsonCodec:
  require(value > 0)
  def increment(): WordCount = WordCount(value + 1)
