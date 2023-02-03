package com.poemdistance.p2p.handler

import com.poemdistance.p2p.command.Command
import com.poemdistance.p2p.command.Command01
import com.poemdistance.p2p.command.Frame
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.util.logging.Logger


@OptIn(ExperimentalUnsignedTypes::class)
class Decoder: ByteToMessageDecoder(){
//     val log = LoggerFactory.getLogger(this.javaClass)
    val log = Logger.getLogger("Decoder");

    override fun decode(ctx: ChannelHandlerContext?, bin: ByteBuf?, out: MutableList<Any>?) {
        if(bin?.isReadable?:return){
            val message = Frame()
            message.data = UByteArray(bin.readableBytes())
            bin.readBytes(message.data.asByteArray())
            if(message.isValid()){
                when(message.type){
                    Command01.TYPE -> out?.add(Command01(message))
                    else -> log.warning("未知命令类型:${message.type}")
                }
            }else{
                log.warning("未接收到符合要求的帧，已关闭通道！")
                ctx?.close()
            }
//            if(Hex.generateSumCheckBit(message.command,2,44) != message.command[46]){
//                log.warn("接收到校验失败数据：${message}")
//                return
//            }

        }
    }
}