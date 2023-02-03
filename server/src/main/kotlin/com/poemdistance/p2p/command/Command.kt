package com.poemdistance.p2p.command

import Hex


@OptIn(ExperimentalUnsignedTypes::class)
open class Frame {
    @OptIn(ExperimentalUnsignedTypes::class)
    var data = ubyteArrayOf()

    /**
     * 帧类型
     */
    open var type:UByte = 0u
        get() {
            if(this.data.isEmpty()) return field
            else return this.data[12]
        }

    open fun isValid(): Boolean{
        return  calcCheckByte(this.data) == data.last()
    }

    fun calcCheckByte(source:UByteArray):UByte{
        var result = 0
        for(index in  0 until source.size-1){
            result += source[index].toInt()
        }
        return (result and 0xFFFFFFFF.toInt()).toUByte()
    }
}
@OptIn(ExperimentalUnsignedTypes::class)
abstract class Command: Frame() {

    /**
     * 帧总长度
     */
    private var length = 0 // 帧总长度
        get() {
            if(this.data.isEmpty()) return field
            else return this.data.size
        }

    /**
     * 当前时间戳
     */
    public var timeStamp:Long = 0
        get() {
            if(this.data.isEmpty()){
                return field
            }
            return Hex.bytesToLong(this.data.asByteArray(), 4,8,false)
        }

    /**
     * 数据信息域
     */
    var dataArea = ubyteArrayOf()
        get() {
            if(this.data.isEmpty()) return field
            else return this.data.sliceArray(13 until this.length-1)
        }

    open fun toArray():UByteArray{

        length = 2 + 2 + 8 + 1 + dataArea.size + 1
        val result = UByteArray(length)
        result[0] = 0x05u
        result[1] = 0x11u
        System.arraycopy(Hex.intToBytes(length, false), 2, result.asByteArray(),2,2)
        this.timeStamp = System.currentTimeMillis()
        System.arraycopy(Hex.longToBytes(this.timeStamp), 0, result.asByteArray(),4,8)
        result[12] = this.type
        System.arraycopy(dataArea.asByteArray(), 0, result.asByteArray(), 13, dataArea.size)
        this.data = result
        result[ 13 + dataArea.size] = this.calcCheckByte(this.data)
        return result
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
class Command01(): Command(){

    companion object{
        val TYPE:UByte = 0x01u
    }
    override var type = TYPE;
    public var code:String = ""
        get() {
            if(this.data.isEmpty()){
                return field
            }else{
                return String(this.dataArea.sliceArray(4 until this.dataArea.size).asByteArray().map {
                    it
                }.toByteArray())
            }
        }

    constructor(frame: Frame) : this() {
        this.data = frame.data
    }

    override fun isValid(): Boolean {
        if(super.isValid()){
            return Hex.bytesToInt(this.data.asByteArray(),13,4,false) == this.calcAuthCode()
        }else{
            return false
        }
    }

    fun calcAuthCode():Int{
        var result = 1
        code.chars().forEach {
            result *=it
        }
        return result
    }

    override fun toArray(): UByteArray {
        this.dataArea = UByteArray(4 + code.length)
        System.arraycopy(Hex.intToBytes(this.calcAuthCode()),0,this.dataArea.asByteArray(),0,4)
        for(index in code.indices){
            this.dataArea[4 + index] = (this.code[index].code).toUByte()
        }
        return super.toArray()

    }
}

@ExperimentalUnsignedTypes
class Command02(): Command(){
    companion object{
        val TYPE:UByte = 0x02u
    }
    override var type = TYPE

    var ip:UByteArray = ubyteArrayOf()
        get() {
            if(this.data.isEmpty()){
                return field
            }else{
                return this.data.sliceArray(13 until 17)
            }
        }

    var port:Int = 0
        get() {
            if(this.data.isEmpty()){
                return field
            }else{
                return Hex.bytesToInt(this.data.asByteArray(),17,2,false)
            }
        }

    var connectTime: Long = 0
        get(){
            if(this.data.isEmpty()){
                return field
            }else{
                return Hex.bytesToLong(this.data.asByteArray(),19,8,false)
            }
        }

    override fun toArray(): UByteArray {
        this.dataArea = UByteArray(14)
        System.arraycopy(ip.asByteArray(),0,this.dataArea.asByteArray(),0, 4)
        System.arraycopy(Hex.intToBytes(port,false),2,this.dataArea.asByteArray(),4,2)
        System.arraycopy(Hex.longToBytes(connectTime),0,dataArea.asByteArray(),6,8)
        return super.toArray()
    }
}