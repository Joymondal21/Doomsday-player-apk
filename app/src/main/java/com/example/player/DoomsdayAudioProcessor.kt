package com.example.player

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.AudioMode

class DoomsdayAudioProcessor(private val context: Context) {

    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        try {
            release()

            virtualizer = Virtualizer(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(1000.toShort()) // 100% 3D spatializer
                    enabled = true
                }
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(800.toShort())
                    enabled = true
                }
            }

            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                setTargetGain(300) // +3dB gain boost
                enabled = true
            }

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioProcessor", "Error attaching audio effects: ${e.message}")
        }
    }

    private var currentBaseGain = 300
    private var currentBoostPercent = 100

    fun setVolumeBoost(boostPercent: Int) {
        currentBoostPercent = boostPercent.coerceIn(100, 200)
        try {
            if (loudnessEnhancer != null) {
                // Boost 100% -> currentBaseGain (0dB gain)
                // Boost 200% -> currentBaseGain + 1500mB (+15dB gain amplification)
                val extraGain = ((currentBoostPercent - 100) * 15).coerceIn(0, 1500)
                val totalGain = currentBaseGain + extraGain
                loudnessEnhancer?.setTargetGain(totalGain)
                loudnessEnhancer?.enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioProcessor", "Error setting volume boost: ${e.message}")
        }
    }

    fun applyMode(mode: AudioMode) {
        try {
            when (mode) {
                AudioMode.DOLBY_ATMOS -> {
                    virtualizer?.setStrength(1000.toShort())
                    virtualizer?.enabled = true
                    bassBoost?.setStrength(600.toShort())
                    bassBoost?.enabled = true
                    currentBaseGain = 400
                }
                AudioMode.STEREO_ENHANCED -> {
                    virtualizer?.setStrength(800.toShort())
                    virtualizer?.enabled = true
                    bassBoost?.setStrength(400.toShort())
                    currentBaseGain = 200
                }
                AudioMode.HI_RES_PASSTHROUGH -> {
                    virtualizer?.enabled = false
                    bassBoost?.enabled = false
                    currentBaseGain = 0
                }
                AudioMode.VOCAL_BOOST -> {
                    virtualizer?.enabled = false
                    bassBoost?.enabled = false
                    currentBaseGain = 600
                }
                AudioMode.BASS_BEAST -> {
                    virtualizer?.enabled = true
                    bassBoost?.setStrength(1000.toShort())
                    bassBoost?.enabled = true
                    currentBaseGain = 500
                }
            }
            setVolumeBoost(currentBoostPercent)
        } catch (e: Exception) {
            Log.e("AudioProcessor", "Error applying audio mode: ${e.message}")
        }
    }

    fun release() {
        try {
            virtualizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
            equalizer?.release()
        } catch (_: Exception) {}
        virtualizer = null
        bassBoost = null
        loudnessEnhancer = null
        equalizer = null
    }
}
