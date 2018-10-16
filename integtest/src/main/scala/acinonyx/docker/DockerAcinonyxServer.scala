package acinonyx.docker

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.DockerClient.LogsParam
import com.spotify.docker.client.messages.{ContainerConfig, HostConfig}
import com.typesafe.scalalogging.LazyLogging

class DockerAcinonyxServer(docker: DockerClient, config: AcinonyxServerConfig) extends LazyLogging {
  val imageName = "acinonyx-server:1.0.0"

  def deploy(): DockerAcinonyxServer = {
    logger.info("Deploy acinonyx server")

    val hostConfig = HostConfig.builder
      .privileged(true)
      .autoRemove(true)
      .build

    val containerCfg = ContainerConfig.builder()
      .image(imageName)
      .hostConfig(hostConfig)
      .exposedPorts(s"${config.port}/tcp")
      .cmd("sh", "-c", "java -cp 'acinonyx-server.jar:lib/*' acinonyx.Main")
      .build()

    val container = docker.createContainer(containerCfg, config.name)
    docker.startContainer(container.id())

    this
  }

  def kill(): Unit = docker.killContainer(config.name)

  def remove(): Unit = docker.removeContainer(config.name)

  def logs(): String = {
    docker
      .logs(config.name, LogsParam.stdout, LogsParam.stderr)
      .readFully()
  }
}

case class AcinonyxServerConfig(name: String, port: Int)
