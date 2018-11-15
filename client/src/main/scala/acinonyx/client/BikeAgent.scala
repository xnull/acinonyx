package acinonyx.client

import acinonyx.api.Bike
import monix.execution.Cancelable
import monix.execution.Scheduler.{global => scheduler}

import scala.concurrent.duration._
import scala.language.higherKinds

class BikeAgent[F[_]](bike: Bike, bikeClient: BikeClient[F], val cancelable: Option[Cancelable] = None) {

  def start(): BikeAgent[F] = {
    val operation = scheduler.scheduleWithFixedDelay(1.seconds, 1.seconds) {
      bikeClient.ping(bike)
    }

    new BikeAgent(bike, bikeClient, Some(operation))
  }
}
