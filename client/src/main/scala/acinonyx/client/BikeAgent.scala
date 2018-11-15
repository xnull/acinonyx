package acinonyx.client

import monix.execution.Cancelable
import monix.execution.Scheduler.{global => scheduler}

import scala.concurrent.duration._
import scala.language.higherKinds

class BikeAgent[F[_]](bikeClient: BikeClient[F], val cancelable: Option[Cancelable] = None) {

  def start(): BikeAgent[F] = {
    val operation = scheduler
      .scheduleWithFixedDelay(3.seconds, 3.seconds) {
        bikeClient.ping()
      }

    new BikeAgent(bikeClient, Some(operation))
  }
}
