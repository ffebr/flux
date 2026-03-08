package model

trait BoardEventErrors:
  val message: String

case class BoardEventPublishError(message: String) extends BoardEventErrors
case class BoardEventConsumptionError(message: String) extends BoardEventErrors
case class BoardEventSubscribeError(message: String) extends BoardEventErrors
