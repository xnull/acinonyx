package acinonyx.client

import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future

object HttpClient extends App {

  val host = "localhost"
  val client: Service[Request, Response] = Http.client.newService(s"$host:8082")
  val request = Request(Method.Get, "/health")
  val response: Future[Response] = client(request)

  start()

  def start() = {
    response.onSuccess { rep: Response =>
      println("GET success: " + rep.contentString)
    }

    response.onFailure { ex =>
      println("GET err: " + ex)
    }

    Thread.sleep(5000)
  }
}
