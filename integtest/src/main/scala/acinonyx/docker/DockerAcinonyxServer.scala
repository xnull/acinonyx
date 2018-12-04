package acinonyx.docker

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.DockerClient.LogsParam
import com.spotify.docker.client.messages.{ContainerConfig, HostConfig, PortBinding}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object DockerAcinonyxServer {
  val ImageName: String = "bynull-docker-acinonyx.bintray.io/acinonyx-server:latest"
}

class DockerAcinonyxServer(docker: DockerClient, config: AcinonyxServerConfig) extends LazyLogging {

  def deploy(): DockerAcinonyxServer = {
    logger.info("Deploy acinonyx server")

    sys.addShutdownHook {
      Try(kill()) match {
        case Failure(exception) =>
          logger.debug(s"Can't stop docker container: ${config.name}")
        case Success(value) =>
          logger.info(s"Docker container stopped: ${config.name}")
      }
    }

    val portBinding = List(PortBinding.of("0.0.0.0", config.port.toString)).asJava
    val portBindings = Map(config.port.toString -> portBinding).asJava

    val hostConfig = HostConfig.builder
      .privileged(true)
      .autoRemove(true)
      .portBindings(portBindings)
      .build

    val containerCfg = ContainerConfig.builder()
      .image(DockerAcinonyxServer.ImageName)
      .hostConfig(hostConfig)
      .exposedPorts(config.port.toString)
      .cmd("sh", "-c", "java -cp 'acinonyx-server.jar:lib/*' acinonyx.Main")
      .build()

    val container = docker.createContainer(containerCfg, config.name)
    docker.startContainer(container.id())

    this
  }

  def kill(): Unit = {
    logger.info(s"Kill docker container: ${config.name}")
    docker.killContainer(config.name)
  }

  def remove(): Unit = docker.removeContainer(config.name)

  def logs(): String = {
    docker
      .logs(config.name, LogsParam.stdout, LogsParam.stderr)
      .readFully()
  }
}

case class AcinonyxServerConfig(name: String, port: Int)
