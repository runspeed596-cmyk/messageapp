package com.iliyadev.springboot.netty.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import net.jpountz.lz4.LZ4Factory
import org.slf4j.LoggerFactory

// ═══════════════════════════════════════════════════════════════════════════════
// 🗜️ LZ4 Frame Codec - Compress/decompress binary WebSocket frames
// Skip compression for payloads < 64 bytes (overhead not worth it)
// Wire format: [4B original size] [compressed data]
// ═══════════════════════════════════════════════════════════════════════════════

private const val MIN_COMPRESS_SIZE: Int = 64
private val LZ4_FACTORY: LZ4Factory = LZ4Factory.fastestInstance()

class Lz4FrameDecoder : MessageToMessageDecoder<BinaryWebSocketFrame>() {
    private val logger = LoggerFactory.getLogger(Lz4FrameDecoder::class.java)
    private val decompressor = LZ4_FACTORY.fastDecompressor()
    override fun decode(ctx: ChannelHandlerContext, msg: BinaryWebSocketFrame, out: MutableList<Any>) {
        val content: ByteBuf = msg.content()
        val readableBytes: Int = content.readableBytes()
        if (readableBytes < 4) {
            out.add(msg.retain())
            return
        }
        val markedReaderIndex: Int = content.readerIndex()
        val originalSize: Int = content.readIntLE()
        if (originalSize <= 0 || originalSize > 10_000_000) {
            content.readerIndex(markedReaderIndex)
            out.add(msg.retain())
            return
        }
        val compressedSize: Int = content.readableBytes()
        val compressedBytes = ByteArray(compressedSize)
        content.readBytes(compressedBytes)
        try {
            val decompressed = ByteArray(originalSize)
            decompressor.decompress(compressedBytes, 0, decompressed, 0, originalSize)
            val buf: ByteBuf = ctx.alloc().buffer(originalSize)
            buf.writeBytes(decompressed)
            out.add(BinaryWebSocketFrame(buf))
        } catch (e: Exception) {
            content.readerIndex(markedReaderIndex)
            out.add(msg.retain())
        }
    }
}

class Lz4FrameEncoder : MessageToMessageEncoder<BinaryWebSocketFrame>() {
    private val compressor = LZ4_FACTORY.fastCompressor()
    override fun encode(ctx: ChannelHandlerContext, msg: BinaryWebSocketFrame, out: MutableList<Any>) {
        val content: ByteBuf = msg.content()
        val readableBytes: Int = content.readableBytes()
        if (readableBytes < MIN_COMPRESS_SIZE) {
            out.add(msg.retain())
            return
        }
        val originalBytes = ByteArray(readableBytes)
        content.readBytes(originalBytes)
        val maxCompressedLen: Int = compressor.maxCompressedLength(readableBytes)
        val compressed = ByteArray(maxCompressedLen)
        val compressedLen: Int = compressor.compress(originalBytes, 0, readableBytes, compressed, 0, maxCompressedLen)
        if (compressedLen >= readableBytes) {
            val buf: ByteBuf = ctx.alloc().buffer(readableBytes)
            buf.writeBytes(originalBytes)
            out.add(BinaryWebSocketFrame(buf))
            return
        }
        val buf: ByteBuf = ctx.alloc().buffer(4 + compressedLen)
        buf.writeIntLE(readableBytes)
        buf.writeBytes(compressed, 0, compressedLen)
        out.add(BinaryWebSocketFrame(buf))
    }
}
