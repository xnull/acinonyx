package acinonyx.client

import java.util.concurrent.TimeUnit

import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.{DefaultFullHttpRequest, HttpHeaderNames, HttpHeaderValues, HttpScheme}
import io.netty.handler.codec.http2.{Http2SecurityUtil, HttpConversionUtil}
import io.netty.handler.ssl.ApplicationProtocolConfig.{Protocol, SelectedListenerFailureBehavior, SelectorFailureBehavior}
import io.netty.handler.ssl._
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.util.{AsciiString, CharsetUtil}

object Http2Client {
  val SSL: Boolean = System.getProperty("ssl") != null
  val HOST: String = System.getProperty("host", "127.0.0.1")
  val PORT: Int = System.getProperty("port", if (SSL) "8443" else "8080").toInt
  val URL: String = System.getProperty("url", "/whatever")
  val URL2: String = System.getProperty("url2")
  val URL2DATA: String = System.getProperty("url2data", "test data!")

  def start(): Unit = {

    val sslCtl = if (SSL) {
      val provider = if (OpenSsl.isAlpnSupported) SslProvider.OPENSSL
      else SslProvider.JDK

      SslContextBuilder.forClient
        .sslProvider(provider)
        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .applicationProtocolConfig(new ApplicationProtocolConfig(
          Protocol.ALPN,
          SelectorFailureBehavior.NO_ADVERTISE,
          SelectedListenerFailureBehavior.ACCEPT,
          ApplicationProtocolNames.HTTP_2,
          ApplicationProtocolNames.HTTP_1_1)
        )
    } else {
      null
    }

    val workerGroup = new NioEventLoopGroup
    //val initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE)
    /*try { // Configure the client.
      val b = new Nothing
      b.group(workerGroup)
      b.channel(classOf[NioSocketChannel])
      b.option(ChannelOption.SO_KEEPALIVE, true)
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
      val scheme = if (SSL) HttpScheme.HTTPS
      else HttpScheme.HTTP
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
      if (URL2 != null) { // Create a simple POST request with a body.
        val request = new DefaultFullHttpRequest(HTTP_1_1, POST, URL2, wrappedBuffer(URL2DATA.getBytes(CharsetUtil.UTF_8)))
        request.headers.add(HttpHeaderNames.HOST, hostName)
        request.headers.add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text, scheme.name)
        request.headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
        request.headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE)
        responseHandler.put(streamId, channel.write(request), channel.newPromise)
      }
      channel.flush
      responseHandler.awaitResponses(5, TimeUnit.SECONDS)
      System.out.println("Finished HTTP/2 request(s)")
      // Wait until the connection is closed.
      channel.close.syncUninterruptibly
    } finally workerGroup.shutdownGracefully*/
  }
}
