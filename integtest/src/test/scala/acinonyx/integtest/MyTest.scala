package acinonyx.integtest

import acinonyx.client.{ClientScheduler, HttpClient, BikeClientAgent, HttpClientConfig}
import acinonyx.docker.{AcinonyxServerConfig, DockerAcinonyxServer}
import com.spotify.docker.client.DefaultDockerClient
import com.twitter.util.Await
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

    val client = new HttpClient(HttpClientConfig())
    new ClientScheduler(new BikeClientAgent(client)).start()

    Thread.sleep(1000000)
    //println(result.contentString)

    Try(server.kill())

    assert(1 == 1)
  }
}
