package acinonyx.server

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.ssl.{ApplicationProtocolNames, ApplicationProtocolNegotiationHandler}

object Http2OrHttpHandler {
  private val MAX_CONTENT_LENGTH = 1024 * 100
}

class Http2OrHttpHandler extends ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {

  override protected def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
    if (ApplicationProtocolNames.HTTP_2 == protocol) {
      ctx.pipeline.addLast(new Http2HandlerBuilder().build())
      return
    }

    throw new IllegalStateException("unknown protocol: " + protocol)
  }
}
