package com.example.trilho.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trilho.audio.TrilhoAudioSynthesizer
import com.example.trilho.audio.TrilhoVibrator
import com.example.trilho.engine.TrilhoEngine
import com.example.trilho.model.*
import com.example.trilho.storage.TrilhoStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TrilhoScreenState {
  MAIN_MENU,
  SHOP,
  RECORDS,
  SETTINGS,
  GAMEPLAY
}

data class GameOverState(
  val distanceMeters: Int,
  val coinsEarned: Int,
  val isNewRecord: Boolean
)

class TrilhoViewModel(application: Application) : AndroidViewModel(application) {
  private val storage = TrilhoStorage(application)
  val audio = TrilhoAudioSynthesizer()
  val vibrator = TrilhoVibrator(application)

  private val _screen = MutableStateFlow(TrilhoScreenState.MAIN_MENU)
  val screen = _screen.asStateFlow()

  private val _settings = MutableStateFlow(storage.loadSettings())
  val settings = _settings.asStateFlow()

  private val _progress = MutableStateFlow(storage.loadProgress())
  val progress = _progress.asStateFlow()

  private val _isPaused = MutableStateFlow(false)
  val isPaused = _isPaused.asStateFlow()

  private val _gameOverState = MutableStateFlow<GameOverState?>(null)
  val gameOverState = _gameOverState.asStateFlow()

  private val _showHowToPlay = MutableStateFlow(false)
  val showHowToPlay = _showHowToPlay.asStateFlow()

  lateinit var engine: TrilhoEngine
    private set

  private var gameLoopJob: Job? = null

  init {
    initEngine()
    applySettingsToAudioAndHaptics(_settings.value)
    audio.startBackgroundMusic()
  }

  private fun initEngine() {
    engine = TrilhoEngine { event ->
      handleEngineEvent(event)
    }
  }

  private fun handleEngineEvent(event: TrilhoEngine.TrilhoEvent) {
    when (event) {
      is TrilhoEngine.TrilhoEvent.LaneSwitch -> {
        audio.playLaneSwitch()
        vibrator.vibrateLight()
      }
      is TrilhoEngine.TrilhoEvent.Jump -> {
        audio.playJump()
      }
      is TrilhoEngine.TrilhoEvent.Slide -> {
        audio.playSlide()
      }
      is TrilhoEngine.TrilhoEvent.CoinCollected -> {
        audio.playCoin()
      }
      is TrilhoEngine.TrilhoEvent.CrystalCollected -> {
        audio.playCrystal()
        vibrator.vibrateLight()
      }
      is TrilhoEngine.TrilhoEvent.PowerUpCollected -> {
        audio.playPowerUp()
        vibrator.vibrateMedium()
      }
      is TrilhoEngine.TrilhoEvent.CrystalAbilityUsed -> {
        audio.playCrystalAbility()
        vibrator.vibrateMedium()
      }
      is TrilhoEngine.TrilhoEvent.ShieldAbsorbed -> {
        audio.playShieldBreak()
        vibrator.vibrateMedium()
      }
      is TrilhoEngine.TrilhoEvent.Crash -> {
        audio.playCrash()
        vibrator.vibrateCrash()
      }
      is TrilhoEngine.TrilhoEvent.GameOver -> {
        val (updatedProgress, isRecord) = storage.saveRunResult(event.distanceMeters, event.coinsEarned)
        _progress.value = updatedProgress
        _gameOverState.value = GameOverState(
          distanceMeters = event.distanceMeters,
          coinsEarned = event.coinsEarned,
          isNewRecord = isRecord
        )
      }
    }
  }

  fun updateSettings(newSettings: TrilhoSettings) {
    _settings.value = newSettings
    storage.saveSettings(newSettings)
    applySettingsToAudioAndHaptics(newSettings)
  }

  private fun applySettingsToAudioAndHaptics(settings: TrilhoSettings) {
    audio.soundVolume = settings.soundVolume
    audio.musicVolume = settings.musicVolume
    vibrator.isEnabled = settings.vibrationEnabled
  }

  fun navigateTo(newScreen: TrilhoScreenState) {
    _screen.value = newScreen
    if (newScreen == TrilhoScreenState.GAMEPLAY) {
      startGameLoop()
    } else {
      stopGameLoop()
    }
  }

  fun startRun() {
    engine.reset()
    _isPaused.value = false
    _gameOverState.value = null
    navigateTo(TrilhoScreenState.GAMEPLAY)
  }

  fun togglePause() {
    _isPaused.value = !_isPaused.value
  }

  fun resumeGame() {
    _isPaused.value = false
  }

  fun setHowToPlayVisible(visible: Boolean) {
    _showHowToPlay.value = visible
  }

  fun equipSkin(skinId: String) {
    val updated = _settings.value.copy(selectedSkinId = skinId)
    updateSettings(updated)
  }

  fun buySkin(skinId: String, cost: Int) {
    val updatedProgress = storage.unlockSkin(skinId, cost)
    if (updatedProgress != null) {
      _progress.value = updatedProgress
      equipSkin(skinId)
      vibrator.vibrateMedium()
    }
  }

  // Touch and Keyboard input triggers
  fun onSwipeLeft() = engine.moveLeft()
  fun onSwipeRight() = engine.moveRight()
  fun onSwipeUp() = engine.jump()
  fun onSwipeDown() = engine.slide()
  fun onUseCrystalAbility() = engine.useCrystalAbility()

  private fun startGameLoop() {
    stopGameLoop()
    gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
      val frameDurationMs = 1000L / TrilhoEngine.TICK_RATE
      while (isActive) {
        val start = System.currentTimeMillis()
        if (!_isPaused.value && _gameOverState.value == null) {
          engine.tick()
        }
        val elapsed = System.currentTimeMillis() - start
        val delayTime = (frameDurationMs - elapsed).coerceAtLeast(1L)
        delay(delayTime)
      }
    }
  }

  private fun stopGameLoop() {
    gameLoopJob?.cancel()
    gameLoopJob = null
  }

  override fun onCleared() {
    super.onCleared()
    stopGameLoop()
    audio.release()
  }
}
