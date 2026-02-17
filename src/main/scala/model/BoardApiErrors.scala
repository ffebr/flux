package model

import zio.http.Response
import zio.http.Status
import zio.json.EncoderOps
import zio.json.JsonCodec

import java.time.Instant

case class ErrorResponse(message: String, cause: Option[String] = None)
    derives JsonCodec
case class SerializationError(message: String = "oops SerializationError")
    derives JsonCodec
