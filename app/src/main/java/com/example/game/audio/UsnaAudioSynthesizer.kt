package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.sin

class UsnaAudioSynthesizer {
  private val sampleRate = 22050
  private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  @Volatile var soundVolume: Float = 0.8f
  @Volatile var musicVolume: Float = 0.5f

  private var musicJob: Job? = null
  @Volatile private var isMusicPlaying = false

  // Pre-generated sound effect PCM buffers
  private val jumpBuffer by lazy { generateSweep(320f, 640f, 0.08f, 0.8f) }
  private val recordStartBuffer by lazy { generateDualTone(520f, 780f, 0.12f, 0.8f) }
  private val recordEndBuffer by lazy { generateSweep(780f, 440f, 0.10f, 0.8f) }
  private val releaseEchoBuffer by lazy { generateChime(listOf(440f, 660f, 880f, 1320f), 0.22f, 0.85f) }
  private val buttonPressBuffer by lazy { generateClick(220f, 0.06f, 0.9f) }
  private val buttonReleaseBuffer by lazy { generateClick(180f, 0.04f, 0.6f) }
  private val doorOpenBuffer by lazy { generateSweep(180f, 360f, 0.16f, 0.7f) }
  private val doorCloseBuffer by lazy { generateSweep(360f, 180f, 0.16f, 0.7f) }
  private val spikeDeathBuffer by lazy { generateNoiseZap(0.22f, 0.9f) }
  private val victoryBuffer by lazy { generateVictoryFanfare() }

  fun playJump() = playSfx(jumpBuffer)
  fun playRecordStart() = playSfx(recordStartBuffer)
  fun playRecordEnd() = playSfx(recordEndBuffer)
  fun playReleaseEcho() = playSfx(releaseEchoBuffer)
  fun playButtonPressed() = playSfx(buttonPressBuffer)
  fun playButtonReleased() = playSfx(buttonReleaseBuffer)
  fun playDoorOpen() = playSfx(doorOpenBuffer)
  fun playDoorClose() = playSfx(doorCloseBuffer)
  fun playSpikeDeath() = playSfx(spikeDeathBuffer)
  fun playVictory() = playSfx(victoryBuffer)

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
        delay((scaledBuffer.size * 1000L / sampleRate) + 50)
        track.stop()
        track.release()
      } catch (e: Exception) {
        // Audio device unavailable or interrupted
      }
    }
  }

  fun startBackgroundMusic() {
    if (isMusicPlaying) return
    isMusicPlaying = true
    musicJob = coroutineScope.launch {
      // Atmospheric 8-step pentatonic puzzle loop (C minor / space vibe: C, Eb, F, G, Bb)
      val melodyFrequencies = floatArrayOf(
        261.63f, 311.13f, 392.00f, 349.23f,
        523.25f, 466.16f, 392.00f, 311.13f
      )
      val bassFrequencies = floatArrayOf(
        130.81f, 130.81f, 116.54f, 116.54f,
        155.56f, 155.56f, 174.61f, 130.81f
      )
      var noteIndex = 0

      while (isActive && isMusicPlaying) {
        if (musicVolume > 0.01f) {
          val mFreq = melodyFrequencies[noteIndex % melodyFrequencies.size]
          val bFreq = bassFrequencies[noteIndex % bassFrequencies.size]
          playSynthMusicNote(mFreq, bFreq, 0.28f, musicVolume * 0.35f)
        }
        noteIndex++
        delay(300L)
      }
    }
  }

  fun stopBackgroundMusic() {
    isMusicPlaying = false
    musicJob?.cancel()
    musicJob = null
  }

  private fun playSynthMusicNote(freqM: Float, freqB: Float, durationSec: Float, vol: Float) {
    try {
      val numSamples = (sampleRate * durationSec).toInt()
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        // Envelope: soft attack, gentle decay
        val envelope = (1.0 - (i.toDouble() / numSamples)) * 0.8
        val sampleM = sin(2.0 * PI * freqM * t)
        val sampleB = sin(2.0 * PI * freqB * t) * 0.6
        val sample = (sampleM + sampleB) * envelope * vol
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
      val envelope = sin(PI * frac) // Smooth bell envelope
      val sample = sin(phase) * envelope * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateDualTone(freq1: Float, freq2: Float, durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val t = i.toDouble() / sampleRate
      val frac = i.toDouble() / numSamples
      val envelope = sin(PI * frac)
      val sample = (sin(2.0 * PI * freq1 * t) + sin(2.0 * PI * freq2 * t)) * 0.5 * envelope * peakVol
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
        val delaySamples = (idx * 0.04f * sampleRate).toInt()
        if (i >= delaySamples) {
          sum += sin(2.0 * PI * f * (t - idx * 0.04))
        }
      }
      val sample = (sum / freqs.size) * decay * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateClick(freq: Float, durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val t = i.toDouble() / sampleRate
      val decay = (1.0 - (i.toDouble() / numSamples))
      val noise = (Math.random() * 2.0 - 1.0) * 0.3
      val tone = sin(2.0 * PI * freq * t) * 0.7
      val sample = (tone + noise) * decay * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateNoiseZap(durationSec: Float, peakVol: Float): ShortArray {
    val numSamples = (sampleRate * durationSec).toInt()
    val buffer = ShortArray(numSamples)
    for (i in 0 until numSamples) {
      val frac = i.toDouble() / numSamples
      val freq = 800f * (1f - frac.toFloat()) + 60f
      val phase = 2.0 * PI * freq * (i.toDouble() / sampleRate)
      val noise = (Math.random() * 2.0 - 1.0) * 0.4
      val sample = (sin(phase) * 0.6 + noise) * (1.0 - frac) * peakVol
      buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
    return buffer
  }

  private fun generateVictoryFanfare(): ShortArray {
    val notes = listOf(261.63f, 329.63f, 392.00f, 523.25f) // C4, E4, G4, C5
    val noteDuration = 0.12f
    val numSamples = (sampleRate * noteDuration * notes.size).toInt()
    val buffer = ShortArray(numSamples)
    val samplesPerNote = (sampleRate * noteDuration).toInt()

    for (n in notes.indices) {
      val freq = notes[n]
      val start = n * samplesPerNote
      val end = (n + 1) * samplesPerNote
      for (i in start until end) {
        val t = (i - start).toDouble() / sampleRate
        val frac = (i - start).toDouble() / samplesPerNote
        val env = sin(PI * frac)
        val sample = sin(2.0 * PI * freq * t) * env * 0.8
        buffer[i] = (sample * 32767).toInt().coerceIn(-32768, 32767).toShort()
      }
    }
    return buffer
  }

  fun release() {
    stopBackgroundMusic()
    coroutineScope.cancel()
  }
}
