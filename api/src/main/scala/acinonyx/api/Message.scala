package acinonyx.api

trait Message[A] {
  val id: MessageId
  val data: A
}

case class MessageId(id: Long)

case class Bike(id: String)

case class HeartbeatMessage(id: MessageId, data: Bike) extends Message[Bike]
