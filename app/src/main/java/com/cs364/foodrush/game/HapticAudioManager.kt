package com.cs364.foodrush.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.cs364.foodrush.data.settings.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin

/**
 * Immutable CS364 Haptic pattern specification (Rubric 1.3 & Core Game Spec).
 * Kept framework-free so pattern timings can be unit-tested on the JVM.
 */
object HapticPatternSpec {
    const val UI_TAP_DURATION_MS = 12L
    const val UI_TAP_AMPLITUDE = 90

    const val CORRECT_MATCH_DURATION_MS = 20L
    const val CORRECT_MATCH_AMPLITUDE = 135

    val SPEED_UP_TIMINGS_MS = longArrayOf(0L, 40L, 60L, 40L)
    val SPEED_UP_AMPLITUDES = intArrayOf(0, 180, 0, 230)

    const val GAME_OVER_DURATION_MS = 400L
    const val GAME_OVER_AMPLITUDE = 255
}

/**
 * Synthesizes 16-bit mono PCM WAV byte arrays for retro 16-bit arcade SFX & BGM
 * without relying on external binary audio files.
 */
internal object PcmWaveSynthesizer {
    const val SAMPLE_RATE_HZ = 22_050

    data class ToneStep(
        val frequencyHz: Float,
        val durationMs: Int,
        val volume: Float = 0.45f
    )

    fun buildCorrectMatchWav(): ByteArray = encodeWav(
        steps = listOf(
            ToneStep(frequencyHz = 1046.50f, durationMs = 55, volume = 0.40f), // C6
            ToneStep(frequencyHz = 1318.51f, durationMs = 85, volume = 0.44f)  // E6
        )
    )

    fun buildSpeedUpWav(): ByteArray = encodeWav(
        steps = listOf(
            ToneStep(frequencyHz = 523.25f, durationMs = 65, volume = 0.40f),  // C5
            ToneStep(frequencyHz = 659.25f, durationMs = 65, volume = 0.42f),  // E5
            ToneStep(frequencyHz = 783.99f, durationMs = 70, volume = 0.44f),  // G5
            ToneStep(frequencyHz = 1046.50f, durationMs = 120, volume = 0.46f) // C6
        )
    )

    fun buildGameOverWav(): ByteArray = encodeWav(
        steps = listOf(
            ToneStep(frequencyHz = 329.63f, durationMs = 110, volume = 0.46f), // E4
            ToneStep(frequencyHz = 261.63f, durationMs = 110, volume = 0.46f), // C4
            ToneStep(frequencyHz = 220.00f, durationMs = 120, volume = 0.46f), // A3
            ToneStep(frequencyHz = 164.81f, durationMs = 200, volume = 0.50f)  // E3
        )
    )

    fun buildUiClickWav(): ByteArray = encodeWav(
        steps = listOf(
            ToneStep(frequencyHz = 880.0f, durationMs = 40, volume = 0.32f) // A5
        )
    )

    fun buildGameplayBgmWav(): ByteArray {
        val melody = listOf(
            // Bar 1 (C major upbeat arcade diner pulse)
            ToneStep(261.63f, 160, 0.16f), ToneStep(329.63f, 160, 0.14f),
            ToneStep(392.00f, 160, 0.16f), ToneStep(523.25f, 160, 0.14f),
            // Bar 2 (G major turnaround)
            ToneStep(392.00f, 160, 0.16f), ToneStep(329.63f, 160, 0.14f),
            ToneStep(293.66f, 160, 0.15f), ToneStep(392.00f, 160, 0.14f),
            // Bar 3 (F -> G lift)
            ToneStep(349.23f, 160, 0.16f), ToneStep(440.00f, 160, 0.14f),
            ToneStep(523.25f, 160, 0.16f), ToneStep(440.00f, 160, 0.14f),
            // Bar 4 (Resolve to C)
            ToneStep(392.00f, 160, 0.16f), ToneStep(329.63f, 160, 0.14f),
            ToneStep(293.66f, 160, 0.14f), ToneStep(261.63f, 160, 0.15f)
        )
        return encodeWav(steps = melody)
    }

    internal fun encodeWav(steps: List<ToneStep>, sampleRateHz: Int = SAMPLE_RATE_HZ): ByteArray {
        val pcmStream = ByteArrayOutputStream()
        for (step in steps) {
            val sampleCount = ((sampleRateHz * step.durationMs) / 1000).coerceAtLeast(1)
            val fadeSamples = (sampleCount / 8).coerceIn(1, 180)
            for (i in 0 until sampleCount) {
                val t = i.toDouble() / sampleRateHz
                val fundamental = sin(2.0 * PI * step.frequencyHz * t)
                val thirdHarmonic = 0.25 * sin(2.0 * PI * step.frequencyHz * 3.0 * t)
                val rawWave = (fundamental + thirdHarmonic).coerceIn(-1.0, 1.0)

                val envelope = when {
                    i < fadeSamples -> i.toDouble() / fadeSamples
                    i > sampleCount - fadeSamples -> (sampleCount - i).toDouble() / fadeSamples
                    else -> 1.0
                }.coerceIn(0.0, 1.0)

                val sampleValue = (rawWave * envelope * step.volume * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                pcmStream.write(sampleValue and 0xFF)
                pcmStream.write((sampleValue shr 8) and 0xFF)
            }
        }

        val pcmBytes = pcmStream.toByteArray()
        val dataSize = pcmBytes.size
        val riffChunkSize = 36 + dataSize
        val byteRate = sampleRateHz * 2 // 16-bit mono = 2 bytes/sample

        return ByteArrayOutputStream(44 + dataSize).apply {
            // RIFF header
            write("RIFF".toByteArray(Charsets.US_ASCII))
            writeIntLe(riffChunkSize)
            write("WAVE".toByteArray(Charsets.US_ASCII))
            // fmt subchunk
            write("fmt ".toByteArray(Charsets.US_ASCII))
            writeIntLe(16) // PCM subchunk size
            writeShortLe(1) // AudioFormat = 1 (PCM)
            writeShortLe(1) // NumChannels = 1 (Mono)
            writeIntLe(sampleRateHz)
            writeIntLe(byteRate)
            writeShortLe(2) // BlockAlign = 2 bytes
            writeShortLe(16) // BitsPerSample = 16
            // data subchunk
            write("data".toByteArray(Charsets.US_ASCII))
            writeIntLe(dataSize)
            write(pcmBytes)
        }.toByteArray()
    }

    private fun ByteArrayOutputStream.writeIntLe(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
        write((value shr 16) and 0xFF)
        write((value shr 24) and 0xFF)
    }

    private fun ByteArrayOutputStream.writeShortLe(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
    }
}

/**
 * Role 3: Centralized Haptics & Audio Manager (Rubric 1.3, 4.4 & Core Spec).
 *
 * - Uses [VibratorManager] on API 31+ with [Vibrator] fallback on API 26–30.
 * - Uses [SoundPool] for low-latency 16-bit arcade SFX and [MediaPlayer] for gameplay BGM.
 * - Observes Role 5 [UserSettings] (`isSoundEnabled`, `isHapticEnabled`).
 * - Immediately pauses/resumes all audio for Activity lifecycle & Pause dialog safety (Rubric 4.4).
 */
class HapticAudioManager(context: Context) {

    private val appContext = context.applicationContext
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val vibrator: Vibrator? = resolveVibrator(appContext)
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSampleIds = ConcurrentHashMap<Int, Boolean>()

    @Volatile
    private var matchSoundId: Int = 0

    @Volatile
    private var speedUpSoundId: Int = 0

    @Volatile
    private var gameOverSoundId: Int = 0

    @Volatile
    private var uiClickSoundId: Int = 0

    @Volatile
    private var bgmFile: File? = null

    private var bgmPlayer: MediaPlayer? = null

    @Volatile
    private var soundEnabled: Boolean = true

    @Volatile
    private var hapticEnabled: Boolean = true

    @Volatile
    private var gameplaySessionActive: Boolean = false

    @Volatile
    private var audioPaused: Boolean = false

    @Volatile
    private var released: Boolean = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSampleIds[sampleId] = true
            }
        }
        prepareAudioAssetsAsync()
    }

    private fun resolveVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun prepareAudioAssetsAsync() {
        ioScope.launch {
            if (released) return@launch
            val audioDir = File(appContext.cacheDir, "foodrush_audio").apply { mkdirs() }
            val matchFile = ensureWaveFile(audioDir, "sfx_match_v1.wav") {
                PcmWaveSynthesizer.buildCorrectMatchWav()
            }
            val speedUpFile = ensureWaveFile(audioDir, "sfx_speed_up_v1.wav") {
                PcmWaveSynthesizer.buildSpeedUpWav()
            }
            val gameOverFile = ensureWaveFile(audioDir, "sfx_game_over_v1.wav") {
                PcmWaveSynthesizer.buildGameOverWav()
            }
            val clickFile = ensureWaveFile(audioDir, "sfx_ui_click_v1.wav") {
                PcmWaveSynthesizer.buildUiClickWav()
            }
            val loopFile = ensureWaveFile(audioDir, "bgm_arcade_loop_v1.wav") {
                PcmWaveSynthesizer.buildGameplayBgmWav()
            }

            if (released) return@launch
            matchSoundId = soundPool.load(matchFile.absolutePath, 1)
            speedUpSoundId = soundPool.load(speedUpFile.absolutePath, 1)
            gameOverSoundId = soundPool.load(gameOverFile.absolutePath, 1)
            uiClickSoundId = soundPool.load(clickFile.absolutePath, 1)
            bgmFile = loopFile

            if (gameplaySessionActive && !audioPaused && soundEnabled) {
                ensureAndStartBgm()
            }
        }
    }

    private inline fun ensureWaveFile(
        dir: File,
        fileName: String,
        generator: () -> ByteArray
    ): File {
        val file = File(dir, fileName)
        if (!file.exists() || file.length() == 0L) {
            FileOutputStream(file).use { out ->
                out.write(generator())
            }
        }
        return file
    }

    fun updateSettings(settings: UserSettings) {
        soundEnabled = settings.isSoundEnabled
        hapticEnabled = settings.isHapticEnabled

        if (!soundEnabled) {
            pauseSoundPlaybackOnly()
        } else if (gameplaySessionActive && !audioPaused) {
            resumeSoundPlaybackOnly()
        }

        if (!hapticEnabled) {
            runCatching { vibrator?.cancel() }
        }
    }

    fun playUiClick() {
        vibrateOneShot(HapticPatternSpec.UI_TAP_DURATION_MS, HapticPatternSpec.UI_TAP_AMPLITUDE)
        playSample(uiClickSoundId, volume = 0.65f)
    }

    fun playCorrectMatch() {
        vibrateOneShot(
            HapticPatternSpec.CORRECT_MATCH_DURATION_MS,
            HapticPatternSpec.CORRECT_MATCH_AMPLITUDE
        )
        playSample(matchSoundId, volume = 0.85f)
    }

    fun playSpeedLevelUp(@Suppress("UNUSED_PARAMETER") newLevel: Int = 2) {
        vibrateWaveform(
            HapticPatternSpec.SPEED_UP_TIMINGS_MS,
            HapticPatternSpec.SPEED_UP_AMPLITUDES
        )
        playSample(speedUpSoundId, volume = 0.95f)
    }

    fun playGameOver() {
        vibrateOneShot(
            HapticPatternSpec.GAME_OVER_DURATION_MS,
            HapticPatternSpec.GAME_OVER_AMPLITUDE
        )
        // Stop looping gameplay music immediately on KO so only the Game Over cue plays.
        pauseBgmPlayer()
        playSample(gameOverSoundId, volume = 1.0f)
    }

    fun startGameplaySession() {
        if (released) return
        gameplaySessionActive = true
        audioPaused = false
        if (soundEnabled) {
            ensureAndStartBgm(resetToStart = true)
        }
    }

    fun pauseAudio() {
        audioPaused = true
        pauseSoundPlaybackOnly()
    }

    fun resumeAudio() {
        audioPaused = false
        if (soundEnabled && gameplaySessionActive) {
            resumeSoundPlaybackOnly()
        }
    }

    fun stopGameplaySession() {
        gameplaySessionActive = false
        audioPaused = false
        pauseBgmPlayer(resetToStart = true)
        runCatching { soundPool.autoPause() }
    }

    fun release() {
        if (released) return
        released = true
        gameplaySessionActive = false
        ioScope.cancel()
        runCatching { vibrator?.cancel() }
        synchronized(this) {
            runCatching {
                bgmPlayer?.stop()
            }
            runCatching {
                bgmPlayer?.release()
            }
            bgmPlayer = null
        }
        runCatching { soundPool.release() }
    }

    private fun pauseSoundPlaybackOnly() {
        runCatching { soundPool.autoPause() }
        pauseBgmPlayer()
    }

    private fun resumeSoundPlaybackOnly() {
        runCatching { soundPool.autoResume() }
        ensureAndStartBgm()
    }

    private fun playSample(sampleId: Int, volume: Float) {
        if (released || !soundEnabled || sampleId == 0) return
        if (loadedSampleIds[sampleId] != true) return
        runCatching {
            soundPool.play(sampleId, volume, volume, 1, 0, 1.0f)
        }
    }

    @Synchronized
    private fun ensureAndStartBgm(resetToStart: Boolean = false) {
        if (released || !soundEnabled || !gameplaySessionActive || audioPaused) return
        val file = bgmFile ?: return
        runCatching {
            val player = bgmPlayer ?: MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(file.absolutePath)
                isLooping = true
                setVolume(0.35f, 0.35f)
                prepare()
            }.also { bgmPlayer = it }

            if (resetToStart) {
                player.seekTo(0)
            }
            if (!player.isPlaying) {
                player.start()
            }
        }
    }

    @Synchronized
    private fun pauseBgmPlayer(resetToStart: Boolean = false) {
        runCatching {
            bgmPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
                if (resetToStart) {
                    player.seekTo(0)
                }
            }
        }
    }

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        if (released || !hapticEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        runCatching {
            val effectiveAmp = if (vib.hasAmplitudeControl()) {
                amplitude.coerceIn(1, 255)
            } else {
                VibrationEffect.DEFAULT_AMPLITUDE
            }
            vib.vibrate(VibrationEffect.createOneShot(durationMs, effectiveAmp))
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        if (released || !hapticEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        runCatching {
            val effect = if (vib.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(timings, -1)
            }
            vib.vibrate(effect)
        }
    }
}
