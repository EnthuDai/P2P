package com.poemdistance.p2p

import com.poemdistance.p2p.handler.Decoder
import com.poemdistance.p2p.handler.Encoder
import com.poemdistance.p2p.handler.Handler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.nio.ByteOrder

class ServerChannelInitializer : ChannelInitializer<SocketChannel>() {



    override fun initChannel(ch: SocketChannel?) {
        //日志系统
        ch?.pipeline()?.addFirst(LoggingHandler(LogLevel.DEBUG))
        //根据长度字段粘包
        ch?.pipeline()?.addLast(LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 512, 3, 2, -5, 0, true))
        ch?.pipeline()?.addLast(Decoder())
        ch?.pipeline()?.addLast(Encoder())
        ch?.pipeline()?.addLast(Handler())
//        ch?.pipeline()?.addLast(APPLICATION_CONTEXT.getBean(BaseHandler::class.java))
//        ch?.pipeline()?.addLast(APPLICATION_CONTEXT.getBean(LockerHandler::class.java))
//        ch?.pipeline()?.addLast(APPLICATION_CONTEXT.getBean(MoveSensorHandler::class.java))
    }

}
