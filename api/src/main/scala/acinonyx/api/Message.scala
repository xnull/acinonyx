package acinonyx.api

trait Message {
  val id: MessageId
}

case class MessageId(id: Long)

case class Bike(id: String)

case class DataMessage[A](id: MessageId, data: A) extends Message

case class HeartbeatMessage(id: MessageId, bike: Bike) extends Message
