package acinonyx.client

import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future

class AcinonyxHttpClient(config: HttpClientConfig) {
  val client: Service[Request, Response] = Http.client.newService(config.endpoint())

  def start(): Future[Response] = {
    val request = Request(Method.Get, "/health")
    val response: Future[Response] = client(request)

    response
  }
}

case class HttpClientConfig(host: String = "localhost", port: Int = 8082) {
  def endpoint(): String = s"$host:$port"
}
