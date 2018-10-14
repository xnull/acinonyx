package acinonyx.server

import com.typesafe.scalalogging.LazyLogging
import io.netty.buffer.Unpooled.{copiedBuffer, unreleasableBuffer}
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeEvent
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2._
import io.netty.util.CharsetUtil

object Http2Handler {
  val RESPONSE_BYTES: ByteBuf = unreleasableBuffer(copiedBuffer("Hello World", CharsetUtil.UTF_8))

  private def http1HeadersToHttp2Headers(request: FullHttpRequest) = {
    val host = request.headers.get(HttpHeaderNames.HOST)
    val http2Headers = new DefaultHttp2Headers().method(HttpMethod.GET.asciiName).path(request.uri).scheme(HttpScheme.HTTP.name)
    if (host != null) http2Headers.authority(host)
    http2Headers
  }
}

class Http2Handler
(
  override val decoder: Http2ConnectionDecoder,
  override val encoder: Http2ConnectionEncoder,
  val initialSettings: Http2Settings
) extends Http2ConnectionHandler(decoder, encoder, initialSettings) with Http2FrameListener with LazyLogging {
  import Http2Handler._

  /**
    * Handles the cleartext HTTP upgrade event. If an upgrade occurred, sends a simple response via HTTP/2
    * on stream 1 (the stream specifically reserved for cleartext HTTP upgrade).
    */
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    evt match {
      case upgradeEvent: UpgradeEvent =>
        onHeadersRead(ctx, 1, http1HeadersToHttp2Headers(upgradeEvent.upgradeRequest), 0, endOfStream = true)
      case _ =>
    }

    super.userEventTriggered(ctx, evt)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    super.exceptionCaught(ctx, cause)
    logger.error("Error", cause)
    ctx.close
  }

  /**
    * Sends a "Hello World" DATA frame to the client.
    */
  private def sendResponse(ctx: ChannelHandlerContext, streamId: Int, payload: ByteBuf): Unit = { // Send a frame for the response status
    val headers = new DefaultHttp2Headers().status(OK.codeAsText)
    encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise)
    encoder.writeData(ctx, streamId, payload, 0, true, ctx.newPromise)
    // no need to call flush as channelReadComplete(...) will take care of it.
  }

  override def onDataRead(ctx: ChannelHandlerContext, streamId: Int, data: ByteBuf, padding: Int, endOfStream: Boolean): Int = {
    val processed = data.readableBytes + padding
    if (endOfStream) {
      sendResponse(ctx, streamId, data.retain)
    }
    processed
  }

  override def onHeadersRead(ctx: ChannelHandlerContext, streamId: Int, headers: Http2Headers, padding: Int, endOfStream: Boolean): Unit = {
    if (endOfStream) {
      val content = ctx.alloc.buffer
      content.writeBytes(RESPONSE_BYTES.duplicate)
      ByteBufUtil.writeAscii(content, " - via HTTP/2")
      sendResponse(ctx, streamId, content)
    }
  }

  override def onHeadersRead(ctx: ChannelHandlerContext, streamId: Int, headers: Http2Headers, streamDependency: Int, weight: Short, exclusive: Boolean, padding: Int, endOfStream: Boolean): Unit = {
    onHeadersRead(ctx, streamId, headers, padding, endOfStream)
  }

  override def onPriorityRead(ctx: ChannelHandlerContext, streamId: Int, streamDependency: Int, weight: Short, exclusive: Boolean): Unit = {
  }

  override def onRstStreamRead(ctx: ChannelHandlerContext, streamId: Int, errorCode: Long): Unit = {
  }

  override def onSettingsAckRead(ctx: ChannelHandlerContext): Unit = {
  }

  override def onSettingsRead(ctx: ChannelHandlerContext, settings: Http2Settings): Unit = {
  }

  override def onPingRead(ctx: ChannelHandlerContext, data: Long): Unit = {
  }

  override def onPingAckRead(ctx: ChannelHandlerContext, data: Long): Unit = {
  }

  override def onPushPromiseRead(ctx: ChannelHandlerContext, streamId: Int, promisedStreamId: Int, headers: Http2Headers, padding: Int): Unit = {
  }

  override def onGoAwayRead(ctx: ChannelHandlerContext, lastStreamId: Int, errorCode: Long, debugData: ByteBuf): Unit = {
  }

  override def onWindowUpdateRead(ctx: ChannelHandlerContext, streamId: Int, windowSizeIncrement: Int): Unit = {
  }

  override def onUnknownFrame(ctx: ChannelHandlerContext, frameType: Byte, streamId: Int, flags: Http2Flags, payload: ByteBuf): Unit = {
  }
}

import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder
import io.netty.handler.codec.http2.Http2ConnectionDecoder
import io.netty.handler.codec.http2.Http2ConnectionEncoder
import io.netty.handler.codec.http2.Http2FrameLogger
import io.netty.handler.codec.http2.Http2Settings

import io.netty.handler.logging.LogLevel.INFO
object Http2HandlerBuilder {
  val logger = new Http2FrameLogger(INFO, classOf[Http2Handler])
}

final class Http2HandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder[Http2Handler, Http2HandlerBuilder] {
  frameLogger(Http2HandlerBuilder.logger)

  override def build(): Http2Handler = super.build

  override protected def build(decoder: Http2ConnectionDecoder, encoder: Http2ConnectionEncoder,
                               initialSettings: Http2Settings): Http2Handler = {
    val handler = new Http2Handler(decoder, encoder, initialSettings)
    frameListener(handler)
    handler
  }
}