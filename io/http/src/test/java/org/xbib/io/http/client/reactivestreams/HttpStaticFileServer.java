package org.xbib.io.http.client.reactivestreams;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;

public final class HttpStaticFileServer {

    static private EventLoopGroup bossGroup;
    static private EventLoopGroup workerGroup;

    public static void start(int port) throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)//
                .channel(NioServerSocketChannel.class)//
                .handler(new LoggingHandler(LogLevel.INFO))//
                .childHandler(new HttpStaticFileServerInitializer());

        b.bind(port).sync().channel();
    }

    public static void shutdown() {
        Future<?> bossFuture = bossGroup.shutdownGracefully();
        Future<?> workerFuture = workerGroup.shutdownGracefully();
        try {
            bossFuture.await();
            workerFuture.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
