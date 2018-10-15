package acinonyx.server

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.util.Await
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._

object HttpServer extends HttpServer(8082)

class HttpServer(port: Int) extends LazyLogging {

  val health: Endpoint[String] =
    get("health") {
      Ok("ok")
    }

  val ping: Endpoint[String] =
    post("bike" :: jsonBody[Bike]) { bike: Bike =>
      //update bike status
      Ok("ok")
    }

  val api: Service[Request, Response] = (health :+: ping)
    .handle {
      case e: Exception => NotFound(e)
    }
    .toServiceAs[Application.Json]

  def start(): ListeningServer = Http.server.serve(s":$port", api)

  def startAndWait(): Unit = Await.ready(start())
}

case class Bike(id: String)
