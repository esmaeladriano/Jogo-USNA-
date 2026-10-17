package com.example.trilho.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.trilho.model.TrilhoProgress
import com.example.trilho.model.TrilhoSettings

class TrilhoStorage(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("trilho_save_data", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_SETTINGS_SOUND_VOL = "trilho_sound_vol"
    private const val KEY_SETTINGS_MUSIC_VOL = "trilho_music_vol"
    private const val KEY_SETTINGS_VIBRATION = "trilho_vibration"
    private const val KEY_SETTINGS_LANGUAGE = "trilho_language"
    private const val KEY_SETTINGS_SKIN = "trilho_selected_skin"

    private const val KEY_TOTAL_COINS = "trilho_total_coins"
    private const val KEY_BEST_DISTANCE = "trilho_best_distance"
    private const val KEY_TOTAL_RUNS = "trilho_total_runs"
    private const val KEY_UNLOCKED_SKINS = "trilho_unlocked_skins"
  }

  fun loadSettings(): TrilhoSettings {
    return TrilhoSettings(
      soundVolume = prefs.getFloat(KEY_SETTINGS_SOUND_VOL, 0.8f),
      musicVolume = prefs.getFloat(KEY_SETTINGS_MUSIC_VOL, 0.5f),
      vibrationEnabled = prefs.getBoolean(KEY_SETTINGS_VIBRATION, true),
      language = prefs.getString(KEY_SETTINGS_LANGUAGE, "pt") ?: "pt",
      selectedSkinId = prefs.getString(KEY_SETTINGS_SKIN, "solaris") ?: "solaris"
    )
  }

  fun saveSettings(settings: TrilhoSettings) {
    prefs.edit()
      .putFloat(KEY_SETTINGS_SOUND_VOL, settings.soundVolume)
      .putFloat(KEY_SETTINGS_MUSIC_VOL, settings.musicVolume)
      .putBoolean(KEY_SETTINGS_VIBRATION, settings.vibrationEnabled)
      .putString(KEY_SETTINGS_LANGUAGE, settings.language)
      .putString(KEY_SETTINGS_SKIN, settings.selectedSkinId)
      .apply()
  }

  fun loadProgress(): TrilhoProgress {
    val unlockedSet = prefs.getStringSet(KEY_UNLOCKED_SKINS, setOf("solaris")) ?: setOf("solaris")
    return TrilhoProgress(
      totalCoins = prefs.getInt(KEY_TOTAL_COINS, 0),
      bestDistanceMeters = prefs.getInt(KEY_BEST_DISTANCE, 0),
      totalRunsPlayed = prefs.getInt(KEY_TOTAL_RUNS, 0),
      unlockedSkinIds = unlockedSet
    )
  }

  fun saveRunResult(distanceMeters: Int, coinsEarned: Int): Pair<TrilhoProgress, Boolean> {
    val current = loadProgress()
    val isNewRecord = distanceMeters > current.bestDistanceMeters
    val newBest = if (isNewRecord) distanceMeters else current.bestDistanceMeters
    val newTotalCoins = current.totalCoins + coinsEarned
    val newTotalRuns = current.totalRunsPlayed + 1

    prefs.edit()
      .putInt(KEY_TOTAL_COINS, newTotalCoins)
      .putInt(KEY_BEST_DISTANCE, newBest)
      .putInt(KEY_TOTAL_RUNS, newTotalRuns)
      .apply()

    val updated = current.copy(
      totalCoins = newTotalCoins,
      bestDistanceMeters = newBest,
      totalRunsPlayed = newTotalRuns
    )
    return Pair(updated, isNewRecord)
  }

  fun unlockSkin(skinId: String, cost: Int): TrilhoProgress? {
    val current = loadProgress()
    if (current.totalCoins < cost) return null
    val newUnlocked = current.unlockedSkinIds.toMutableSet().apply { add(skinId) }
    val newCoins = current.totalCoins - cost

    prefs.edit()
      .putInt(KEY_TOTAL_COINS, newCoins)
      .putStringSet(KEY_UNLOCKED_SKINS, newUnlocked)
      .apply()

    return current.copy(totalCoins = newCoins, unlockedSkinIds = newUnlocked)
  }
}
