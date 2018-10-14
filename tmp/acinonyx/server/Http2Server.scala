package acinonyx.server

import com.typesafe.scalalogging.LazyLogging
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, ChannelOption, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpServerUpgradeHandler.{UpgradeCodec, UpgradeCodecFactory}
import io.netty.handler.codec.http.{HttpMessage, HttpObjectAggregator, HttpServerCodec, HttpServerUpgradeHandler}
import io.netty.handler.codec.http2._
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.ApplicationProtocolConfig.{Protocol, SelectedListenerFailureBehavior, SelectorFailureBehavior}
import io.netty.handler.ssl._
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.util.{AsciiString, ReferenceCountUtil}


/**
  * https://netty.io/4.1/xref/io/netty/example/http2/helloworld/server/package-summary.html
  */
class Http2Server extends LazyLogging {
  val SSL: Boolean = System.getProperty("ssl") != null
  val PORT: Int = Integer.parseInt(System.getProperty("port", if (SSL) "8443" else "8080"))

  def start(): ServerBootstrap = {

    val group = new NioEventLoopGroup()

    try {
      val b = new ServerBootstrap()
      b.option(ChannelOption.SO_BACKLOG, Integer.valueOf(1024))
      b.group(group)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new Http2ServerInitializer(sslCtx()))

      b.bind(PORT).sync().channel()
      b
    } finally {
      group.shutdownGracefully()
    }
  }

  private def sslCtx(): Option[SslContext] = {
    if (SSL) {
      val provider = if (OpenSsl.isAlpnSupported) SslProvider.OPENSSL else SslProvider.JDK
      val ssc = new SelfSignedCertificate()

      val ssl = SslContextBuilder
        .forServer(ssc.certificate(), ssc.privateKey())
        .sslProvider(provider)
        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
        .applicationProtocolConfig(new ApplicationProtocolConfig(
          Protocol.ALPN,
          // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
          SelectorFailureBehavior.NO_ADVERTISE,
          // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
          SelectedListenerFailureBehavior.ACCEPT,
          ApplicationProtocolNames.HTTP_2,
          ApplicationProtocolNames.HTTP_1_1))
        .build()

      Some(ssl)
    } else {
      Option.empty
    }
  }
}

class Http2ServerInitializer(sslCtx: Option[SslContext], maxHttpContentLength: Int = 16 * 1024)
  extends ChannelInitializer[SocketChannel] with LazyLogging {

  val upgradeCodecFactory: UpgradeCodecFactory = (protocol: CharSequence) => {
    if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
      new Http2ServerUpgradeCodec(new Http2HandlerBuilder().build())
    } else {
      null
    }
  }

  override def initChannel(ch: SocketChannel): Unit = {
    sslCtx match {
      case Some(ssl) =>
        configureSsl(ch)
      case None =>
        configureClearText(ch)
    }
  }

  private def configureSsl(ch: SocketChannel): Unit = {
    sslCtx.foreach { ssl =>
      ch.pipeline.addLast(ssl.newHandler(ch.alloc), new Http2OrHttpHandler())
    }
  }

  private def configureClearText(ch: SocketChannel): Unit = {
    val p = ch.pipeline
    val sourceCodec = new HttpServerCodec
    val upgradeHandler = new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory)
    val cleartextHttp2ServerUpgradeHandler = new CleartextHttp2ServerUpgradeHandler(
      sourceCodec,
      upgradeHandler,
      new Http2HandlerBuilder().build()
    )

    p.addLast(cleartextHttp2ServerUpgradeHandler)
    p.addLast(new UserEventLogger())
  }
}

import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

private class UserEventLogger extends ChannelInboundHandlerAdapter with LazyLogging {
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    logger.info(s"User Event Triggered: $evt")
    ctx.fireUserEventTriggered(evt)
  }
}
