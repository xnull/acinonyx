package acinonyx.integtest

import acinonyx.docker.{AcinonyxServerConfig, DockerAcinonyxServer}
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

    Try(server.remove())
    Try(server.deploy())
    Thread.sleep(300000)
    println(server.logs())

    Try(server.kill())

    assert(1 == 1)
  }
}
