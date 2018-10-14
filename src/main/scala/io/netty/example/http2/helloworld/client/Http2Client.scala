package io.netty.example.http2.helloworld.client

import java.util.concurrent.TimeUnit

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.HttpMethod.GET
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http.{DefaultFullHttpRequest, HttpHeaderNames, HttpHeaderValues, HttpScheme}
import io.netty.handler.codec.http2.{Http2SecurityUtil, HttpConversionUtil}
import io.netty.handler.ssl.ApplicationProtocolConfig.{Protocol, SelectedListenerFailureBehavior, SelectorFailureBehavior}
import io.netty.handler.ssl._
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.AsciiString

/**
  * An HTTP2 client that allows you to send HTTP2 frames to a server. Inbound and outbound frames are
  * logged. When run from the command-line, sends a single HEADERS frame to the server and gets back
  * a "Hello World" response.
  */
object Http2Client extends App {
  private[client] val HOST = System.getProperty("host", "127.0.0.1")
  private[client] val PORT = System.getProperty("port", "8443").toInt
  private[client] val URL = System.getProperty("url", "/whatever")

  val provider = if (OpenSsl.isAlpnSupported) SslProvider.OPENSSL
  else SslProvider.JDK
  val sslCtx = SslContextBuilder.forClient()
    .sslProvider(provider)
    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
    .trustManager(InsecureTrustManagerFactory.INSTANCE)
    .applicationProtocolConfig(new ApplicationProtocolConfig(
      Protocol.ALPN,
      SelectorFailureBehavior.NO_ADVERTISE,
      SelectedListenerFailureBehavior.ACCEPT,
      ApplicationProtocolNames.HTTP_2,
      ApplicationProtocolNames.HTTP_1_1))
    .build()


  val workerGroup = new NioEventLoopGroup
  val initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE)
  try { // Configure the client.
    val b = new Bootstrap
    b.group(workerGroup)
    b.channel(classOf[NioSocketChannel])
    b.option(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
    b.remoteAddress(HOST, PORT)
    b.handler(initializer)
    // Start the client.
    val channel = b.connect.syncUninterruptibly.channel
    System.out.println("Connected to [" + HOST + ':' + PORT + ']')
    // Wait for the HTTP/2 upgrade to occur.
    val http2SettingsHandler = initializer.settingsHandler
    http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS)
    val responseHandler = initializer.responseHandler
    var streamId = 3
    val scheme = HttpScheme.HTTPS
    val hostName = new AsciiString(HOST + ':' + PORT)
    System.err.println("Sending request(s)...")
    if (URL != null) { // Create a simple GET request.
      val request = new DefaultFullHttpRequest(HTTP_1_1, GET, URL)
      request.headers.add(HttpHeaderNames.HOST, hostName)
      request.headers.add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text, scheme.name)
      request.headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
      request.headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE)
      responseHandler.put(streamId, channel.write(request), channel.newPromise)
      streamId += 2
    }
    channel.flush
    responseHandler.awaitResponses(5, TimeUnit.SECONDS)
    System.out.println("Finished HTTP/2 request(s)")
    // Wait until the connection is closed.
    channel.close.syncUninterruptibly
  } finally {
    workerGroup.shutdownGracefully()
  }

}

final class Http2Client {}