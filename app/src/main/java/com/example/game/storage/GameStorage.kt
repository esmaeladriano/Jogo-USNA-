package com.example.game.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.GameProgress
import com.example.game.model.GameSettings
import com.example.game.model.LevelStats
import org.json.JSONObject

class GameStorage(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("usna_save_data", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_SETTINGS_SOUND_VOL = "settings_sound_vol"
    private const val KEY_SETTINGS_MUSIC_VOL = "settings_music_vol"
    private const val KEY_SETTINGS_VIBRATION = "settings_vibration"
    private const val KEY_SETTINGS_LANGUAGE = "settings_language"
    private const val KEY_SETTINGS_SKIN = "settings_skin"
    private const val KEY_PROGRESS_DATA = "progress_json"
  }

  fun loadSettings(): GameSettings {
    return GameSettings(
      soundVolume = prefs.getFloat(KEY_SETTINGS_SOUND_VOL, 0.8f),
      musicVolume = prefs.getFloat(KEY_SETTINGS_MUSIC_VOL, 0.5f),
      vibrationEnabled = prefs.getBoolean(KEY_SETTINGS_VIBRATION, true),
      language = prefs.getString(KEY_SETTINGS_LANGUAGE, "pt") ?: "pt",
      selectedSkinId = prefs.getString(KEY_SETTINGS_SKIN, "solar") ?: "solar"
    )
  }

  fun saveSettings(settings: GameSettings) {
    prefs.edit()
      .putFloat(KEY_SETTINGS_SOUND_VOL, settings.soundVolume)
      .putFloat(KEY_SETTINGS_MUSIC_VOL, settings.musicVolume)
      .putBoolean(KEY_SETTINGS_VIBRATION, settings.vibrationEnabled)
      .putString(KEY_SETTINGS_LANGUAGE, settings.language)
      .putString(KEY_SETTINGS_SKIN, settings.selectedSkinId)
      .apply()
  }

  fun loadProgress(): GameProgress {
    val jsonStr = prefs.getString(KEY_PROGRESS_DATA, null)
    if (jsonStr.isNullOrEmpty()) {
      return GameProgress(mapOf(1 to LevelStats(unlocked = true)))
    }

    return try {
      val jsonObj = JSONObject(jsonStr)
      val statsMap = mutableMapOf<Int, LevelStats>()
      val keys = jsonObj.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        val levelId = key.toIntOrNull() ?: continue
        val obj = jsonObj.getJSONObject(key)
        statsMap[levelId] = LevelStats(
          unlocked = obj.optBoolean("unlocked", false),
          stars = obj.optInt("stars", 0),
          bestTimeSec = obj.optDouble("bestTimeSec", 0.0).toFloat(),
          echoesUsed = obj.optInt("echoesUsed", 0)
        )
      }
      if (!statsMap.containsKey(1)) {
        statsMap[1] = LevelStats(unlocked = true)
      }
      GameProgress(statsMap)
    } catch (_: Exception) {
      GameProgress(mapOf(1 to LevelStats(unlocked = true)))
    }
  }

  fun saveProgress(progress: GameProgress) {
    val jsonObj = JSONObject()
    for ((levelId, stats) in progress.levelStats) {
      val obj = JSONObject().apply {
        put("unlocked", stats.unlocked)
        put("stars", stats.stars)
        put("bestTimeSec", stats.bestTimeSec.toDouble())
        put("echoesUsed", stats.echoesUsed)
      }
      jsonObj.put(levelId.toString(), obj)
    }
    prefs.edit().putString(KEY_PROGRESS_DATA, jsonObj.toString()).apply()
  }

  fun updateLevelCompletion(levelId: Int, starsEarned: Int, timeSec: Float, echoesUsed: Int): GameProgress {
    val current = loadProgress().levelStats.toMutableMap()
    val prevStats = current[levelId] ?: LevelStats(unlocked = true)
    val bestStars = kotlin.math.max(prevStats.stars, starsEarned)
    val bestTime = if (prevStats.bestTimeSec > 0.01f) minOf(prevStats.bestTimeSec, timeSec) else timeSec

    current[levelId] = LevelStats(
      unlocked = true,
      stars = bestStars,
      bestTimeSec = bestTime,
      echoesUsed = echoesUsed
    )

    // Unlock next level (up to 10)
    val nextLevelId = levelId + 1
    if (nextLevelId <= 10) {
      val nextStats = current[nextLevelId] ?: LevelStats(unlocked = false)
      current[nextLevelId] = nextStats.copy(unlocked = true)
    }

    val updatedProgress = GameProgress(current)
    saveProgress(updatedProgress)
    return updatedProgress
  }
}
