package com.iliyadev.springboot.netty.codec

import com.iliyadev.springboot.netty.protocol.BinaryReader
import com.iliyadev.springboot.netty.protocol.Envelope
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 FlatBuffers Binary Protocol Codec
// Decodes BinaryWebSocketFrame → Envelope (opCode + payload)
// Zero-copy: reads directly from Netty ByteBuf without intermediate allocation
// ═══════════════════════════════════════════════════════════════════════════════

class FlatBuffersDecoder : MessageToMessageDecoder<BinaryWebSocketFrame>() {
    override fun decode(ctx: ChannelHandlerContext, msg: BinaryWebSocketFrame, out: MutableList<Any>) {
        val content: ByteBuf = msg.content()
        val readableBytes: Int = content.readableBytes()
        if (readableBytes < 5) return
        val bytes = ByteArray(readableBytes)
        content.readBytes(bytes)
        val reader = BinaryReader(bytes)
        val envelope: Envelope = Envelope.deserialize(reader)
        out.add(envelope)
    }
}
