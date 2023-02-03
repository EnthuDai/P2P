package com.poemdistance.p2p

import com.poemdistance.p2p.handler.Decoder
import com.poemdistance.p2p.handler.Encoder
import com.poemdistance.p2p.handler.Handler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.net.InetSocketAddress
import java.nio.ByteOrder
import java.util.logging.Logger

class NettyServer {
//    val log = LoggerFactory.getLogger(this.javaClass)

    val log = Logger.getLogger("Decoder");


    fun start(port:Int){
        val inetSocketAddress = InetSocketAddress("0.0.0.0", port)

        //new 一个主线程组
        val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
        //new 一个工作线程组
        val workGroup: EventLoopGroup = NioEventLoopGroup(200)
        val bootstrap = ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(ServerChannelInitializer())
            .localAddress(inetSocketAddress)
            .option(ChannelOption.SO_BACKLOG, 1024) // 队列大小
            .childOption(ChannelOption.SO_KEEPALIVE, true)
        //绑定端口,开始接收进来的连接
        try {
            val future = bootstrap.bind(inetSocketAddress).sync()
            log.info("服务器启动开始监听端口: ${inetSocketAddress.port}" )
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            //关闭主线程组
            bossGroup.shutdownGracefully()
            //关闭工作线程组
            workGroup.shutdownGracefully()
        }
    }
}
