package com.poemdistance.p2p.handler

import com.poemdistance.p2p.command.Command01
import com.poemdistance.p2p.command.Command02
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Logger

@OptIn(ExperimentalUnsignedTypes::class)
data class ClientInfo(var ip:UByteArray, var port:Int, var ctx:ChannelHandlerContext, var connectTime:Long)

object Container{

    val lock = ReentrantLock()

    var map: ConcurrentHashMap<String, ClientInfo> = ConcurrentHashMap()

    fun containsKey(key:String):Boolean{
        return map.containsKey(key)
    }

    fun remove(key:String):ClientInfo?{
        if(map.size > 100) clear() // 可优化
        return map.remove(key)
    }

    fun put(key:String, client:ClientInfo){
        map[key] = client
    }

    fun get(key:String):ClientInfo?{
        return map[key]
    }

    /**
     * 删除超时的客户端连接
     */
    fun clear(){
        map.forEach { (t, u) ->
            if(u.connectTime + 60*1000 < System.currentTimeMillis()){
                map.remove(t)
            }
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
class Handler: ChannelInboundHandlerAdapter() {
    val log = Logger.getLogger("Handler")
    /**
     * 匹配码
     */
    lateinit var code: String
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        Container.lock.lock() // 当两个配对连接同时连入时，需要进行同步控制，否则都会发现连接库中找不到同code的连接
        try{
            if(msg is Command01){
                val address =  ctx.channel().remoteAddress() as InetSocketAddress
                val ipStr = address.address.hostAddress
                val ip = arrayListOf<UByte>()
                log.info("收到来自ip:$ipStr,port:${address.port},发送时间：${msg.timeStamp}，延迟${System.currentTimeMillis() - msg.timeStamp}ms")
                ipStr.split(".").forEach {
                    ip.add(it.toUByte())
                }
                this.code = msg.code
                if(Container.containsKey(code)){
                    val connectTime = System.currentTimeMillis() + 3000
                    val first = Command02().apply {
                        this.ip = ip.toUByteArray()
                        this.port = address.port
                        this.connectTime = connectTime
                    }
                    val firstClient = Container.remove(msg.code)
                    val second = Command02().apply{
                        this.ip = firstClient!!.ip
                        this.port = firstClient.port
                        this.connectTime = connectTime
                    }
                    val writeFeature1 = firstClient?.ctx?.writeAndFlush(first)
                    val writeFeature2 = ctx.writeAndFlush(second)
                    writeFeature1?.get()
                    firstClient?.ctx?.close()
                    writeFeature2?.get()
                    ctx.close()
                }else{
                    Container.put(msg.code, ClientInfo(ip.toUByteArray(), address.port, ctx, System.currentTimeMillis()))
                }
            }else{
                ctx.fireChannelRead(msg)
            }
        }finally {
            Container.lock.unlock()
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if(::code.isInitialized){
            Container.remove(code)
        }
        super.channelInactive(ctx)
    }
}
