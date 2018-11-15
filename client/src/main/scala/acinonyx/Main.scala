package acinonyx

import acinonyx.api.Bike
import acinonyx.client.{BikeAgent, BikeClientIo, HttpClient, HttpClientConfig}


object Main extends App {

  val bike = Bike("333")
  val clientConfig = HttpClientConfig()
  val httpClient = new HttpClient(clientConfig)
  new BikeAgent(bike, new BikeClientIo(httpClient)).start()

}
