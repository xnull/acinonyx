package acinonyx

import java.util.concurrent.TimeUnit

import acinonyx.server.Http2Server

object Main extends App {

  System.setProperty("ssl", "true")
  val server = new Http2Server()
  server.start()

  TimeUnit.HOURS.sleep(24)
}
