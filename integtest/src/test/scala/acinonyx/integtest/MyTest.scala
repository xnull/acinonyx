package acinonyx.integtest

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.ContainerConfig
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MyTest extends FunSuite{

  test("check"){
    val docker = DefaultDockerClient.fromEnv().build()

    val cfg = ContainerConfig.builder()
      .image("acinonyx-client:1.0.0")
      .build()

    val container = docker.createContainer(cfg)
    docker.startContainer(container.id())

    assert(1 == 1)
  }
}
