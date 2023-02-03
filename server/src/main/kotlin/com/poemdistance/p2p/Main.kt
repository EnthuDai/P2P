package com.poemdistance.p2p

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    NettyServer().start(22569)
}
