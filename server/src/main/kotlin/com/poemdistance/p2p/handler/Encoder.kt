package com.poemdistance.p2p.handler

import com.poemdistance.p2p.command.Command
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.util.CharsetUtil
import java.util.logging.Logger

@OptIn(ExperimentalUnsignedTypes::class)
class Encoder: MessageToByteEncoder<Command>() {
    val log = Logger.getLogger("Decoder");

    override fun encode(ctx: ChannelHandlerContext?, msg: Command?, out: ByteBuf?) {
        out?.writeBytes(Unpooled.copiedBuffer(msg?.toArray()?.asByteArray()))
        log.info("encoder write a message ${msg?.toArray()?.toList()}")

//        out?.writeBytes(msg?.data)
    }

}