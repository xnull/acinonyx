package acinonyx.client

import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service}

import scala.concurrent.Future
import scala.language.higherKinds


class HttpClient(config: HttpClientConfig) {
  private val client: Service[Request, Response] = Http.client.newService(config.endpoint())

  def send(f: Service[Request, Response] => Future[Response]): Future[Response] = f.apply(client)

}

case class HttpClientConfig(host: String = "localhost", port: Int = 8082) {
  def endpoint(): String = s"$host:$port"
}
