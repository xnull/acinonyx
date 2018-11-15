package acinonyx

import acinonyx.server.AcinonyxServer
import com.twitter.util.Await

object Main extends App {

  val server = AcinonyxServer.start()
  Await.ready(server)
}
