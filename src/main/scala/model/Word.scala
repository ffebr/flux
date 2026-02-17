package model

import zio.json.JsonCodec
import zio.json.JsonFieldDecoder
import zio.json.JsonFieldEncoder

case class Word(value: String) derives JsonCodec:
  require(!value.isBlank())
  def normalize(): Word = Word(value.trim().toLowerCase())

object Word:
  given JsonFieldEncoder[Word] = JsonFieldEncoder[String].contramap(_.value)
  given JsonFieldDecoder[Word] = JsonFieldDecoder[String].map(Word.apply)
