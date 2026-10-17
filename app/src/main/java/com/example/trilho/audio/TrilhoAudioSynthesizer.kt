package com.example.trilho.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

class TrilhoAudioSynthesizer {
  private val sampleRate = 22050
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  @Volatile var soundVolume: Float = 0.8f
  @Volatile var musicVolume: Float = 0.5f

  private var musicJob: Job? = null
  @Volatile private var isMusicPlaying = false

  // Pre-generated Sound Effect Buffers
  private val laneSwitchBuffer by lazy { generateSweep(280f, 440f, 0.05f, 0.7f) }
  private val jumpBuffer by lazy { generateSweep(300f, 650f, 0.09f, 0.85f) }
  private val slideBuffer by lazy { generateSweep(400f, 200f, 0.08f, 0.75f) }
  private val coinBuffer by lazy { generateDualTone(987.77f, 1318.51f, 0.08f, 0.7f) } // B5 + E6
  private val crystalBuffer by lazy { generateChime(listOf(659.25f, 987.77f, 1318.51f), 0.16f, 0.8f) }
  private val powerUpBuffer by lazy { generateChime(listOf(523.25f, 659.25f, 783.99f, 1046.50f), 0.22f, 0.85f) }
  private val crystalAbilityBuffer by lazy { generateSweep(350f, 900f, 0.18f, 0.9f) }
  private val shieldBreakBuffer by lazy { generateNoiseZap(0.15f, 0.8f) }
  private val crashBuffer by lazy { generateNoiseZap(0.35f, 1.0f) }

  fun playLaneSwitch() = playSfx(laneSwitchBuffer)
  fun playJump() = playSfx(jumpBuffer)
  fun playSlide() = playSfx(slideBuffer)
  fun playCoin() = playSfx(coinBuffer)
  fun playCrystal() = playSfx(crystalBuffer)
  fun playPowerUp() = playSfx(powerUpBuffer)
  fun playCrystalAbility() = playSfx(crystalAbilityBuffer)
  fun playShieldBreak() = playSfx(shieldBreakBuffer)
  fun playCrash() = playSfx(crashBuffer)

  private fun playSfx(buffer: ShortArray) {
    if (soundVolume <= 0.01f) return
    coroutineScope.launch {
      try {
        val scaledBuffer = ShortArray(buffer.size)
        val vol = soundVolume
        for (i in buffer.indices) {
          scaledBuffer[i] = (buffer[i] * vol).toInt().coerceIn(-32768, 32767).toShort()
        }

        val track = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(scaledBuffer.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        track.write(scaledBuffer, 0, scaledBuffer.size)
        track.play()
        delay((scaledBuffer.size * 1000L / sampleRate) + 40)
        track.stop()
        track.release()
      } catch (_: Exception) {}
    }
  }

  fun startBackgroundMusic() {
    if (isMusicPlaying) return
    isMusicPlaying = true
    musicJob = coroutineScope.launch {
      // High-energy 8-step techno bassline for runner adrenaline
      val bassNotes = floatArrayOf(
        110f, 110f, 130.81f, 110f,
        146.83f, 130.81f, 164.81f, 146.83f
      )
      val leadNotes = floatArrayOf(
        440f, 523.25f, 659.25f, 587.33f,
        523.25f, 659.25f, 783.99f, 659.25f
      )
      var step = 0

      while (isActive && isMusicPlaying) {
        if (musicVolume > 0.01f) {
          val bFreq = bassNotes[step % bassNotes.size]
          val lFreq = leadNotes[step % leadNotes.size]
          playSynthRunnerBeat(bFreq, lFreq, 0.18f, musicVolume * 0.38f)
        }
        step++
        delay(200L) // 150 BPM energetic tempo
      }
    }
  }

  fun stopBackgroundMusic() {
    isMusicPlaying = false
    musicJob?.cancel()
    musicJob = null
  }

  private fun playSynthRunnerBeat(bassFreq: Float, leadFreq: Float, durationSec: Float, vol: Float) {
    try {
      val numSamples = (sampleRate * durationSec).toInt()
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val env = 1.0 - (i.toDouble() / numSamples)
        val sBass = sin(2.0 * PI * bassFreq * t) * 0.7
        val sLead = sin(2.0 * PI * leadFreq * t) * 0.4
        val sample = (sBass + sLead) * env * vol
        buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
      }

      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(buffer, 0, buffer.size)
      track.play()
      coroutineScope.launch {
        delay((durationSec * 1000).toLong() + 20)
        try {
          track.stop()
          track.release()
        } catch (_: Exception) {}
      }
    } catch (_: Exception) {}
  }

  private fun generateSweep(startFreq: Float, endFreq: Float, durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    var phase = 0.0
    for (i in 0 until numSamples) {
      val frac = i.toDouble() / numSamples
      val currentFreq = startFreq + (endFreq - startFreq) * frac
      phase += 2.0 * PI * currentFreq / sampleRate
      val env = sin(PI * frac)
      val sample = sin(phase) * env * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateDualTone(f1: Float, f2: Float, durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val t = i.toDouble() / sampleRate
      val frac = i.toDouble() / numSamples
      val env = sin(PI * frac)
      val sample = (sin(2.0 * PI * f1 * t) + sin(2.0 * PI * f2 * t)) * 0.5 * env * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateChime(freqs: List<Float>, durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val t = i.toDouble() / sampleRate
      val frac = i.toDouble() / numSamples
      val decay = 1.0 - frac
      var sum = 0.0
      for ((idx, f) in freqs.withIndex()) {
        val delaySamples = (idx * 0.035f * sampleRate).toInt()
        if (i >= delaySamples) {
          sum += sin(2.0 * PI * f * (t - idx * 0.035))
        }
      }
      val sample = (sum / freqs.size) * decay * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateNoiseZap(durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val frac = i.toDouble() / numSamples
      val noise = (Math.random() * 2.0 - 1.0) * 0.6
      val freq = 500f * (1f - frac.toFloat()) + 60f
      val phase = 2.0 * PI * freq * (i.toDouble() / sampleRate)
      val sample = (sin(phase) * 0.4 + noise) * (1.0 - frac) * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  fun release() {
    stopBackgroundMusic()
    coroutineScope.cancel()
  }
}
