package com.example.game.audio

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class UsnaVibrator(context: Context) {
  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  var isEnabled: Boolean = true

  fun vibrateClick() {
    vibratePattern(30L, 80)
  }

  fun vibrateAction() {
    vibratePattern(50L, 160)
  }

  fun vibrateHazard() {
    vibratePattern(150L, 255)
  }

  fun vibrateVictory() {
    if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val timings = longArrayOf(0, 60, 60, 80, 60, 120)
        val amplitudes = intArrayOf(0, 100, 0, 180, 0, 255)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200L)
      }
    } catch (_: Exception) {}
  }

  private fun vibratePattern(durationMs: Long, amplitude: Int) {
    if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMs)
      }
    } catch (_: Exception) {}
  }
}
