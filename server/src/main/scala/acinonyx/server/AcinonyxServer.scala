package acinonyx.server

import acinonyx.api.{Bike, HeartbeatMessage}
import cats.effect.IO
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.finch.Endpoint._
import io.finch._
import io.finch.circe._

object AcinonyxServer extends AcinonyxServer(8082)

class AcinonyxServer(port: Int) extends LazyLogging with Module[IO] {

  val health: Endpoint[IO, String] = get("health") {
    Ok("ok")
  }

  val ping: Endpoint[IO, Bike] =
    post("ping" :: jsonBody[HeartbeatMessage]) { message: HeartbeatMessage =>
      //update bike status
      Ok(message.data)
    }

  val api: Service[Request, Response] = (health :+: ping)
    .handle {
      case e: Exception => NotFound(e)
    }
    .toServiceAs[Application.Json]

  def start(): ListeningServer = {
    val server = Http.server.serve(s":$port", api)
    server
  }
}
