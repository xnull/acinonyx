package acinonyx.client

import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import monix.execution.Cancelable
import monix.execution.Scheduler.{global => scheduler}

import scala.concurrent.duration._

class AcinonyxClientScheduler(client: AcinonyxHttpClient, private val cancelable: Option[Cancelable] = None) {

  def start(): AcinonyxClientScheduler = {
    val operation = scheduler
      .scheduleWithFixedDelay(3.seconds, 3.seconds) {
        val r = Await.result(client.send())
        println(s"ping: ${r.contentString}")
      }

    new AcinonyxClientScheduler(client, Some(operation))
  }
}

class AcinonyxHttpClient(config: HttpClientConfig) {
  val client: Service[Request, Response] = Http.client.newService(config.endpoint())

  /**
    * Стартуем клиента и он по таймеру делает пинги на сервак что всё ок!!!
    * Надо консюмер
    *
    * - Велик стоит на балконе
    * - начинается замес
    * - велик понимает что надо послать сигнал тревоги на сервак
    * - и начинает трекать свое местоположение и посылать координаты на сервак
    * - получает уведомления от сервака? Какие и зачем?
    * - послыает заряд на сервак, видно сколько осталось батарейки
    *
    * - стриминг не стриминг это пох,
    *    - сервак к нему подсоединяется мобила
    *    - подсоединяется клиент
    *    - клиент спрашивает есть ли сообщения для него - например что велик потерян может прийти от сервака
    *    - или подать звуковой сигнал - тоже от сервака
    *    - на серваке надо мапу - для такого-то клиента такое-то сообщение
    *
    * @return
    */
  def send(): Future[Response] = {

    val request = Request(Method.Get, "/health")
    val response: Future[Response] = client(request)

    response
  }
}

case class HttpClientConfig(host: String = "localhost", port: Int = 8082) {
  def endpoint(): String = s"$host:$port"
}
