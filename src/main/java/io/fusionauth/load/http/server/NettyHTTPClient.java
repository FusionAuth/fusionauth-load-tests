/*
 * Copyright (c) 2022, FusionAuth, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package io.fusionauth.load.http.server;

import java.io.Closeable;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class NettyHTTPClient implements Closeable {
  private final EventLoopGroup group;

  public NettyHTTPClient() {
    group = new NioEventLoopGroup();
  }

  @Override
  public void close() {
    // Shut down executor threads to exit.
    group.shutdownGracefully();
  }

  public boolean go() {
    // Configure the client.
    try {
      HttpClientInitializer init = new HttpClientInitializer();
      Bootstrap b = new Bootstrap();
      b.group(group)
       .channel(NioSocketChannel.class)
       .handler(init);

      // Make the connection attempt.
      Channel ch = b.connect("127.0.0.1", 9011).sync().channel();

      // Prepare the HTTP request.
      HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/api/system/version", Unpooled.EMPTY_BUFFER);
      request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
      request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

      // Send the HTTP request.
      ch.writeAndFlush(request);

      // Wait for the server to close the connection.
      ch.closeFuture().sync();
      return init.handler.success;
    } catch (Exception e) {
      return false;
    }
  }

  public static class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    public boolean success;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
      if (msg instanceof FullHttpResponse response) {
        success = response.status() == HttpResponseStatus.OK;
      }

      ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      cause.printStackTrace();
      ctx.close();
    }
  }

  public static class HttpClientInitializer extends ChannelInitializer<SocketChannel> {
    public HttpClientHandler handler = new HttpClientHandler();

    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();

      p.addLast(new HttpClientCodec());

      // Remove the following line if you don't want automatic content decompression.
      p.addLast(new HttpContentDecompressor());

      // Uncomment the following line if you don't want to handle HttpContents.
      p.addLast(new HttpObjectAggregator(1048576));

      p.addLast(handler);
    }
  }
}
