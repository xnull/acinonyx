package acinonyx.client

import acinonyx.api.{Bike, HeartbeatMessage, MessageId}
import cats.effect.IO
import com.twitter.bijection.Conversion._
import com.twitter.bijection.twitter_util.UtilBijections.twitter2ScalaFuture
import com.twitter.finagle.http.Response
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.parser.decode
import io.finch.circe._
import io.finch.{Application, Input}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.higherKinds

trait BikeClient[F[_]] {
  def health(): F[Response]

  def ping(bike: Bike): F[Bike]
}

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
class BikeClientIo(client: HttpClient) extends BikeClient[IO] with LazyLogging {

  def health(): IO[Response] = {
    logger.trace("Health check")

    val response = client.send { endpoint =>
      endpoint(Input.get("/health").request).as[Future[Response]]
    }

    for {
      status <- IO.fromFuture(IO.pure(response))
    } yield {
      status
    }
  }

  def ping(bike: Bike): IO[Bike] = {
    logger.info("Heartbeat bike: {}", bike)

    val message = HeartbeatMessage(MessageId(123), bike)

    val response = client.send { endpoint =>
      val input = Input
        .post("/ping")
        .withBody[Application.Json](message)

      endpoint(input.request).as[Future[Response]]
    }

    val bikeIo = for {
      resp <- IO.fromFuture(IO.pure(response))
      bike <- IO.fromEither(decode[Bike](resp.contentString))
    } yield {
      logger.info("Bike ping: {}", bike)
      bike
    }

    bikeIo.unsafeRunSync()

    bikeIo
  }
}
