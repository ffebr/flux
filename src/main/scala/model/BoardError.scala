package model

sealed trait BoardError:
  val message: String

case object BoardExpiredError extends BoardError:
  val message: String = ""
