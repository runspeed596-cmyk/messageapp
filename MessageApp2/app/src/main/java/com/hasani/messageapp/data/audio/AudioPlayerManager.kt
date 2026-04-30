package com.hasani.messageapp.data.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AudioPlayerManager handles audio playback using ExoPlayer (Media3).
 * Singleton pattern ensures only one audio plays at a time.
 * Uses OkHttpDataSource for authenticated media access.
 */

// ═══════════════════════════════════════════════════════════════════════════════
// 🎵 Playback State
// ═══════════════════════════════════════════════════════════════════════════════

enum class PlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class PlaybackInfo(
    val state: PlaybackState = PlaybackState.IDLE,
    val currentUrl: String? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val progress: Float = 0f,
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎵 Audio Player Manager
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "AudioPlayerManager"
        private const val PROGRESS_UPDATE_INTERVAL_MS = 100L
    }

    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playbackInfo = MutableStateFlow(PlaybackInfo())
    val playbackInfo: StateFlow<PlaybackInfo> = _playbackInfo.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playbackInfo.value = _playbackInfo.value.copy(
                        state = PlaybackState.LOADING
                    )
                }
                Player.STATE_READY -> {
                    val duration = exoPlayer?.duration ?: 0L
                    if (exoPlayer?.isPlaying == true) {
                        _playbackInfo.value = _playbackInfo.value.copy(
                            state = PlaybackState.PLAYING,
                            durationMs = duration
                        )
                        startProgressUpdates()
                    } else {
                        _playbackInfo.value = _playbackInfo.value.copy(
                            state = PlaybackState.PAUSED,
                            durationMs = duration
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    stopProgressUpdates()
                    _playbackInfo.value = _playbackInfo.value.copy(
                        state = PlaybackState.COMPLETED,
                        currentPositionMs = _playbackInfo.value.durationMs,
                        progress = 1f
                    )
                }
                Player.STATE_IDLE -> {
                    stopProgressUpdates()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                _playbackInfo.value = _playbackInfo.value.copy(state = PlaybackState.PLAYING)
                startProgressUpdates()
            } else if (_playbackInfo.value.state == PlaybackState.PLAYING) {
                _playbackInfo.value = _playbackInfo.value.copy(state = PlaybackState.PAUSED)
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            Log.e(TAG, "Playback error: ${error.message}", error)
            stopProgressUpdates()
            _playbackInfo.value = _playbackInfo.value.copy(
                state = PlaybackState.ERROR,
                error = error.message ?: "Playback error"
            )
        }
    }

    /**
     * Play audio from the given URL.
     * Stops any currently playing audio first.
     */
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun play(url: String) {
        Log.i(TAG, "▶️ Playing: $url")

        // Stop current playback if different URL
        if (_playbackInfo.value.currentUrl != url) {
            stop()
        }

        // Resume if same URL and paused
        if (_playbackInfo.value.currentUrl == url && _playbackInfo.value.state == PlaybackState.PAUSED) {
            exoPlayer?.play()
            return
        }

        // Initialize player if needed with OkHttpDataSource for authenticated requests
        if (exoPlayer == null) {
            val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)
            
            exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    addListener(playerListener)
                }
        }

        _playbackInfo.value = PlaybackInfo(
            state = PlaybackState.LOADING,
            currentUrl = url
        )

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }

    /**
     * Pause playback.
     */
    fun pause() {
        Log.i(TAG, "⏸️ Pausing")
        exoPlayer?.pause()
    }

    /**
     * Toggle play/pause for the given URL.
     */
    fun togglePlayPause(url: String) {
        if (_playbackInfo.value.currentUrl == url) {
            if (_playbackInfo.value.state == PlaybackState.PLAYING) {
                pause()
            } else {
                play(url)
            }
        } else {
            play(url)
        }
    }

    /**
     * Stop playback and reset.
     */
    fun stop() {
        Log.i(TAG, "⏹️ Stopping")
        stopProgressUpdates()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _playbackInfo.value = PlaybackInfo(state = PlaybackState.IDLE)
    }

    /**
     * Seek to a specific position.
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        updateProgress()
    }

    /**
     * Seek to a percentage of the duration.
     * @param progress Progress value from 0 to 1
     */
    fun seekToProgress(progress: Float) {
        val duration = exoPlayer?.duration ?: return
        val positionMs = (duration * progress.coerceIn(0f, 1f)).toLong()
        seekTo(positionMs)
    }

    /**
     * Check if the given URL is currently playing.
     */
    fun isPlaying(url: String): Boolean {
        return _playbackInfo.value.currentUrl == url && 
               _playbackInfo.value.state == PlaybackState.PLAYING
    }

    /**
     * Release player resources.
     */
    fun release() {
        stopProgressUpdates()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
        _playbackInfo.value = PlaybackInfo(state = PlaybackState.IDLE)
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (true) {
                updateProgress()
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateProgress() {
        val player = exoPlayer ?: return
        val position = player.currentPosition
        val duration = player.duration.coerceAtLeast(1L)
        val progress = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

        _playbackInfo.value = _playbackInfo.value.copy(
            currentPositionMs = position,
            durationMs = duration,
            progress = progress
        )
    }
}
