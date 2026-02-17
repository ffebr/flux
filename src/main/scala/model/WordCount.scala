package model

import zio.json.JsonCodec
import zio.json.JsonFieldDecoder
import zio.json.JsonFieldEncoder

case class WordCount(value: Int) derives JsonCodec:
  require(value > 0)
  def increment(): WordCount = WordCount(value + 1)

object WordCount:
  given JsonFieldEncoder[WordCount] = JsonFieldEncoder[Int].contramap(_.value)
  given JsonFieldDecoder[WordCount] = JsonFieldDecoder[Int].map(WordCount.apply)
