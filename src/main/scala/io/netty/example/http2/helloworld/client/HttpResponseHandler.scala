package io.netty.example.http2.helloworld.client

import java.util
import java.util.concurrent.{ConcurrentMap, TimeUnit}

import io.netty.channel.{ChannelFuture, ChannelHandlerContext, ChannelPromise, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http2.HttpConversionUtil
import io.netty.util.CharsetUtil
import io.netty.util.internal.PlatformDependent

/**
  * Process {@link io.netty.handler.codec.http.FullHttpResponse} translated from HTTP/2 frames
  */
class HttpResponseHandler() extends SimpleChannelInboundHandler[FullHttpResponse] {
  val streamidPromiseMap: ConcurrentMap[Int, util.Map.Entry[ChannelFuture, ChannelPromise]] = PlatformDependent.newConcurrentHashMap

  /**
    * Create an association between an anticipated response stream id and a {@link io.netty.channel.ChannelPromise}
    *
    * @param streamId    The stream for which a response is expected
    * @param writeFuture A future that represent the request write operation
    * @param promise     The promise object that will be used to wait/notify events
    * @return The previous object associated with { @code streamId}
    * @see HttpResponseHandler#awaitResponses(long, java.util.concurrent.TimeUnit)
    */
  def put(streamId: Int, writeFuture: ChannelFuture, promise: ChannelPromise): util.Map.Entry[ChannelFuture, ChannelPromise] = {
    streamidPromiseMap.put(
      streamId,
      new util.AbstractMap.SimpleEntry[ChannelFuture, ChannelPromise](writeFuture, promise)
    )
  }

  /**
    * Wait (sequentially) for a time duration for each anticipated response
    *
    * @param timeout Value of time to wait for each response
    * @param unit    Units associated with { @code timeout}
    * @see HttpResponseHandler#put(int, io.netty.channel.ChannelFuture, io.netty.channel.ChannelPromise)
    */
  def awaitResponses(timeout: Long, unit: TimeUnit): Unit = {
    val itr = streamidPromiseMap.entrySet.iterator
    while ( {
      itr.hasNext
    }) {
      val entry = itr.next
      val writeFuture = entry.getValue.getKey
      if (!writeFuture.awaitUninterruptibly(timeout, unit)) throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey)
      if (!writeFuture.isSuccess) throw new RuntimeException(writeFuture.cause)
      val promise = entry.getValue.getValue
      if (!promise.awaitUninterruptibly(timeout, unit)) throw new IllegalStateException("Timed out waiting for response on stream id " + entry.getKey)
      if (!promise.isSuccess) throw new RuntimeException(promise.cause)
      System.out.println("---Stream id: " + entry.getKey + " received---")
      itr.remove()
    }
  }

  override protected def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse): Unit = {
    val streamId = msg.headers.getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text)
    if (streamId == null) {
      System.err.println("HttpResponseHandler unexpected message received: " + msg)
      return
    }
    //В зависимости от типа мессаджа забубенить код! Клиент!
    val entry = streamidPromiseMap.get(streamId)
    if (entry == null) System.err.println("Message received for unknown stream id " + streamId)
    else { // Do stuff with the message (for now just print it)
      val content = msg.content
      if (content.isReadable) {
        val contentLength = content.readableBytes
        val arr = new Array[Byte](contentLength)
        content.readBytes(arr)
        System.out.println(new String(arr, 0, contentLength, CharsetUtil.UTF_8))
      }
      entry.getValue.setSuccess()
    }
  }
}