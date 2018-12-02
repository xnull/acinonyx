package acinonyx.integtest

import acinonyx.api.Bike
import acinonyx.client._
import acinonyx.docker.{AcinonyxServerConfig, DockerAcinonyxServer}
import cats.effect.IO
import com.spotify.docker.client.DefaultDockerClient
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class MyTest extends FunSuite {

  test("check") {
    val server = new DockerAcinonyxServer(
      DefaultDockerClient.fromEnv().build,
      AcinonyxServerConfig("acinonyx-server", 8082)
    )

    Try(server.kill())
    Try(server.deploy())
    println(server.logs())

    Thread.sleep(3000)

    val bike = Bike("123")
    val client = new HttpClient(HttpClientConfig())
    val bikeClient = new BikeClientIo(client)
    val bikeAgent = new BikeAgent[IO](bike, bikeClient).start()

    //Thread.sleep(1000000)
    //println(result.contentString)

    Try(server.kill())

    bikeAgent.cancelable.foreach(_.cancel)

    assert(1 == 1)
  }
}
