package com.poemdistance

import com.poemdistance.command.Command01
import com.poemdistance.command.Command02
import com.poemdistance.handler.Decoder
import com.poemdistance.handler.Encoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.net.InetSocketAddress
import java.nio.ByteOrder
import java.util.logging.Logger
import kotlin.concurrent.thread


@OptIn(ExperimentalUnsignedTypes::class)
fun main(args: Array<String>) {
    Client().init()
    Client().init()

//    val serverPort = 22569
//    val socket = Socket("127.0.0.1", serverPort)
//    val localPort = socket.localPort
//    socket.reuseAddress = true
//    val loginCommand = Command01().apply {
//        this.code = "1234"
//    }
//    println(loginCommand.toArray().toList())
//    socket.getOutputStream().write(loginCommand.toArray().asByteArray())
//
//    val tmp = ByteArray(64)
//    var len = socket.getInputStream().read(tmp)
//    println(tmp.toUByteArray().toList().toString())


}


class Client{

    fun init(){
        ServerClient().connect("127.0.0.1", 22569)
    }


}

interface ClientEvent{

    /**
     * 与服务器建立连接后触发
     */
    fun onServerConnected(ctx: ChannelHandlerContext)

    /**
     * 当与服务器断开连接后触发
     */
    fun onServerDisconnected(ctx: ChannelHandlerContext)

    /**
     * 当收到配对信息后触发
     */
    fun onPairInfoReceived(ctx: ChannelHandlerContext, host:String, port:Int, delay:Long)

    /**
     * 当成功建立P2P连接后触发
     */
    fun onP2PConnected(ctx:ChannelHandlerContext)

    /**
     * P2P连接失败后触发
     */
    fun onP2PConnectFailed(cause: String)
}

class ServerClient:ClientEvent{
    private val serverGroup = NioEventLoopGroup()
    private val b = Bootstrap()
    var port : Int = 0
    fun connect(ip:String, port:Int){
        b.group(serverGroup).channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_REUSEADDR, true)
            .handler(ServerChannelInitializer(this))
        thread{
            val future = b.connect(ip, port).sync()
            future.channel().closeFuture().sync()
            println("与服务器的通道已关闭！")
        }
    }
    fun disconnect(){
        serverGroup.shutdownGracefully();
    }

    override fun onServerConnected(ctx: ChannelHandlerContext) {
        port = (ctx.channel().localAddress() as InetSocketAddress).port
    }

    override fun onServerDisconnected(ctx: ChannelHandlerContext) {
        TODO("Not yet implemented")
    }

    override fun onPairInfoReceived(ctx: ChannelHandlerContext, host: String, port: Int, delay: Long) {
        if(delay > 0) {
            Thread.sleep(delay)
        }
        val bootstrap = Bootstrap()
        bootstrap.group(serverGroup).channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_REUSEADDR, true)
            .handler(P2PChannelInitializer(this)).bind(this.port)
        Thread{
            bootstrap.connect(host, port).sync()
        }
    }

    override fun onP2PConnected(ctx: ChannelHandlerContext) {
        TODO("Not yet implemented")
    }

    override fun onP2PConnectFailed(cause: String) {
        TODO("Not yet implemented")
    }
}



class ServerChannelInitializer(val listener:ClientEvent) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel?) {
        //日志系统
        ch?.pipeline()?.addFirst(LoggingHandler(LogLevel.DEBUG))
        //根据长度字段粘包
        ch?.pipeline()?.addLast(LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 512, 3, 2, -5, 0, true))
        ch?.pipeline()?.addLast(Decoder())
        ch?.pipeline()?.addLast(Encoder())
        ch?.pipeline()?.addLast(ServerHandler(listener))
    }
}

class P2PChannelInitializer(val listener:ClientEvent) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel?) {
        //日志系统
        ch?.pipeline()?.addFirst(LoggingHandler(LogLevel.DEBUG))
        //根据长度字段粘包
        ch?.pipeline()?.addLast(LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 65536, 3, 2, -5, 0, true))
        ch?.pipeline()?.addLast(Decoder())
        ch?.pipeline()?.addLast(Encoder())
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
class ServerHandler(val listener:ClientEvent): ChannelInboundHandlerAdapter() {
    val log = Logger.getLogger("ServerHandler");
    override fun channelActive(ctx: ChannelHandlerContext) {
        val loginCommand = Command01().apply {
            this.code = "1234"
        }
        ctx.writeAndFlush(loginCommand)
        super.channelActive(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if(msg is Command02){
            log.info("目标IP：${msg.ip}, 端口：${msg.port}， 距离预定连接时间还有：${msg.connectTime - System.currentTimeMillis()}ms")
            listener.onPairInfoReceived(ctx, msg.ip.joinToString("."),msg.port, msg.connectTime - System.currentTimeMillis())
        }else{
            super.channelRead(ctx, msg)
        }
    }
}

class P2PClient{

}



