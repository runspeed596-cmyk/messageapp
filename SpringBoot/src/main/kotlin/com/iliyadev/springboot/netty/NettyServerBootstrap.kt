package com.iliyadev.springboot.netty

import com.iliyadev.springboot.config.JwtTokenUtils
import com.iliyadev.springboot.netty.codec.FlatBuffersDecoder
import com.iliyadev.springboot.netty.codec.Lz4FrameDecoder
import com.iliyadev.springboot.netty.codec.Lz4FrameEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.timeout.IdleStateHandler
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

// ═══════════════════════════════════════════════════════════════════════════════
// 🚀 Netty WebSocket Server Bootstrap
// Optimized for 100K concurrent connections within 8GB RAM
// - Boss group: 2 threads (accept connections)
// - Worker group: 14 threads (handle IO)
// - PooledByteBufAllocator (minimize GC pressure)
// - TCP_NODELAY, SO_KEEPALIVE, SO_BACKLOG=1024
// ═══════════════════════════════════════════════════════════════════════════════

@Component
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class NettyServerBootstrap(
    private val connectionManager: ConnectionManager,
    private val messagePipeline: MessagePipelineService,
    private val jwtTokenUtils: JwtTokenUtils,
    @Value("\${netty.port:9090}") private val port: Int,
    @Value("\${netty.boss-threads:2}") private val bossThreads: Int,
    @Value("\${netty.worker-threads:14}") private val workerThreads: Int
) {
    private val logger = LoggerFactory.getLogger(NettyServerBootstrap::class.java)
    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup
    private var channel: Channel? = null

    @PostConstruct
    fun start() {
        Thread {
            try {
                bossGroup = NioEventLoopGroup(bossThreads)
                workerGroup = NioEventLoopGroup(workerThreads)
                val bootstrap = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(16 * 1024, 64 * 1024))
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            ch.pipeline()
                                .addLast("idle", IdleStateHandler(60, 30, 0, TimeUnit.SECONDS))
                                .addLast("http-codec", HttpServerCodec())
                                .addLast("http-aggregator", HttpObjectAggregator(65536))
                                .addLast("ws-protocol", WebSocketServerProtocolHandler("/ws", null, true, 65536))
                                .addLast("lz4-decoder", Lz4FrameDecoder())
                                .addLast("lz4-encoder", Lz4FrameEncoder())
                                .addLast("flatbuf-decoder", FlatBuffersDecoder())
                                .addLast("ws-handler", WebSocketFrameHandler(connectionManager, messagePipeline, jwtTokenUtils))
                        }
                    })
                val future: ChannelFuture = bootstrap.bind(port).sync()
                channel = future.channel()
                logger.info("🚀 Netty WebSocket server started on port $port (boss=$bossThreads, workers=$workerThreads)")
                future.channel().closeFuture().sync()
            } catch (e: Exception) {
                logger.error("❌ Netty server failed to start: ${e.message}", e)
            } finally {
                shutdown()
            }
        }.apply {
            name = "netty-server"
            isDaemon = true
            start()
        }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("🔌 Shutting down Netty server...")
        channel?.close()
        if (::bossGroup.isInitialized) bossGroup.shutdownGracefully()
        if (::workerGroup.isInitialized) workerGroup.shutdownGracefully()
        messagePipeline.shutdown()
    }
}
