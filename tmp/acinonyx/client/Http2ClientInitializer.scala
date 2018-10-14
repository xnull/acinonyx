package acinonyx.client

import acinonyx.client.Http2ClientInitializer.UserEventLogger
import com.typesafe.scalalogging.LazyLogging
import io.netty.channel.socket.SocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer, ChannelPipeline}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2._
import io.netty.handler.logging.LogLevel.INFO
import io.netty.handler.ssl.{ApplicationProtocolNames, ApplicationProtocolNegotiationHandler, SslContext}

object Http2ClientInitializer extends LazyLogging {
  private val logger = new Http2FrameLogger(INFO, classOf[Http2ClientInitializer])

  /**
    * Class that logs any User Events triggered on this channel.
    */
  private class UserEventLogger extends ChannelInboundHandlerAdapter with LazyLogging {
    override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
      logger.info(s"User Event Triggered: $evt")
      ctx.fireUserEventTriggered(evt)
    }
  }

}

class Http2ClientInitializer(val sslCtx: SslContext, val maxContentLength: Int) extends ChannelInitializer[SocketChannel] {

  import acinonyx.client.Http2ClientInitializer
  import io.netty.handler.codec.http2.Http2FrameLogger

  val logger = new Http2FrameLogger(INFO, classOf[Http2ClientInitializer])
  private var connectionHandler: HttpToHttp2ConnectionHandler = null
  private var responseHandler: HttpResponseHandler = null
  private var settingsHandler: Http2SettingsHandler = null

  override def initChannel(ch: SocketChannel): Unit = {
    val connection = new DefaultHttp2Connection(false)
    connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
      .frameListener(new DelegatingDecompressorFrameListener(
        connection,
        new InboundHttp2ToHttpAdapterBuilder(connection)
          .maxContentLength(maxContentLength)
          .propagateSettings(true)
          .build()))
      .frameLogger(logger)
      .connection(connection)
      .build()

    responseHandler = new HttpResponseHandler()
    settingsHandler = new Http2SettingsHandler(ch.newPromise())

    if (sslCtx != null) {
      configureSsl(ch)
    }
    else {
      configureClearText(ch)
    }
  }

  protected def configureEndOfPipeline(pipeline: ChannelPipeline): Unit = {
    pipeline.addLast(settingsHandler, responseHandler)
  }

  /**
    * Configure the pipeline for TLS NPN negotiation to HTTP/2.
    */
  private def configureSsl(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline
    pipeline.addLast(sslCtx.newHandler(ch.alloc))
    // We must wait for the handshake to finish and the protocol to be negotiated before configuring
    // the HTTP/2 components of the pipeline.
    pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
      override protected def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
        if (ApplicationProtocolNames.HTTP_2 == protocol) {
          val p = ctx.pipeline
          p.addLast(connectionHandler)
          configureEndOfPipeline(p)
          return
        }
        ctx.close
        throw new IllegalStateException("unknown protocol: " + protocol)
      }
    })
  }

  /**
    * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
    */
  private def configureClearText(ch: SocketChannel): Unit = {
    val sourceCodec = new HttpClientCodec
    val upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler)
    val upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536)
    ch.pipeline.addLast(
      sourceCodec,
      upgradeHandler,
      new UpgradeRequestHandler(),
      new UserEventLogger()
    )
  }

  /**
    * A handler that triggers the cleartext upgrade to HTTP/2 by sending an initial HTTP request.
    */
  final private class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {

    override def channelActive(ctx: ChannelHandlerContext): Unit = {
      val upgradeRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
      ctx.writeAndFlush(upgradeRequest)
      ctx.fireChannelActive
      // Done with this handler, remove it from the pipeline.
      ctx.pipeline.remove(this)
      configureEndOfPipeline(ctx.pipeline)
    }
  }

}
