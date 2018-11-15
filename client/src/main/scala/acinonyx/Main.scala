package acinonyx

import acinonyx.client.{ClientScheduler, HttpClient, BikeClientAgent, HttpClientConfig}


object Main extends App {

  val clientConfig = HttpClientConfig()
  val httpClient = new HttpClient(clientConfig)
  new ClientScheduler(new BikeClientAgent(httpClient)).start()

}
