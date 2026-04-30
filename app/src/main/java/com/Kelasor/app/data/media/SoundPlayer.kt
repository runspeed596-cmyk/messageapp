package com.Kelasor.app.data.media

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.Kelasor.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔊 Sound Player for message send/receive sounds
 * 
 * Provides audio feedback for message interactions.
 */
@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SoundPlayer"
    }
    
    private var sendSoundPlayer: MediaPlayer? = null
    private var receiveSoundPlayer: MediaPlayer? = null
    private var isSoundEnabled: Boolean = true
    
    /**
     * Play send message sound — DISABLED per user request
     */
    fun playSendSound() {
        // Sound disabled
    }
    
    /**
     * Play receive message sound — DISABLED per user request
     */
    fun playReceiveSound() {
        // Sound disabled
    }
    
    /**
     * Enable or disable sounds
     */
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }
    
    /**
     * Check if sound is enabled
     */
    fun isSoundEnabled(): Boolean = isSoundEnabled
    
    private fun releaseSendPlayer() {
        try {
            sendSoundPlayer?.release()
            sendSoundPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing send player", e)
        }
    }
    
    private fun releaseReceivePlayer() {
        try {
            receiveSoundPlayer?.release()
            receiveSoundPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing receive player", e)
        }
    }
    
    /**
     * Release all resources
     */
    fun release() {
        releaseSendPlayer()
        releaseReceivePlayer()
    }
}
