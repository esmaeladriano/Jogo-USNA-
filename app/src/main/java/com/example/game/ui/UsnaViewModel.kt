package com.example.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.UsnaAudioSynthesizer
import com.example.game.audio.UsnaVibrator
import com.example.game.engine.UsnaLevels
import com.example.game.engine.UsnaPhysicsEngine
import com.example.game.model.*
import com.example.game.storage.GameStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ScreenState {
  MAIN_MENU,
  LEVEL_SELECT,
  SETTINGS,
  GAMEPLAY
}

data class VictoryState(
  val level: LevelData,
  val timeSec: Float,
  val stars: Int,
  val echoesUsed: Int
)

class UsnaViewModel(application: Application) : AndroidViewModel(application) {
  private val storage = GameStorage(application)
  val audio = UsnaAudioSynthesizer()
  val vibrator = UsnaVibrator(application)

  private val _screen = MutableStateFlow(ScreenState.MAIN_MENU)
  val screen = _screen.asStateFlow()

  private val _currentLevelId = MutableStateFlow(1)
  val currentLevelId = _currentLevelId.asStateFlow()

  private val _settings = MutableStateFlow(storage.loadSettings())
  val settings = _settings.asStateFlow()

  private val _progress = MutableStateFlow(storage.loadProgress())
  val progress = _progress.asStateFlow()

  private val _isPaused = MutableStateFlow(false)
  val isPaused = _isPaused.asStateFlow()

  private val _victoryState = MutableStateFlow<VictoryState?>(null)
  val victoryState = _victoryState.asStateFlow()

  private val _showHowToPlay = MutableStateFlow(false)
  val showHowToPlay = _showHowToPlay.asStateFlow()

  private val _showSkins = MutableStateFlow(false)
  val showSkins = _showSkins.asStateFlow()

  // Hardware/Touch combined input
  private var currentInput = GameInput()

  // Physics engine instance
  lateinit var engine: UsnaPhysicsEngine
    private set

  private var gameLoopJob: Job? = null

  init {
    val initialLevel = UsnaLevels.getLevel(_currentLevelId.value)
    initEngine(initialLevel)
    applySettingsToAudioAndHaptics(_settings.value)
    audio.startBackgroundMusic()
  }

  private fun initEngine(level: LevelData) {
    engine = UsnaPhysicsEngine(level) { event ->
      handleEngineEvent(event)
    }
  }

  private fun handleEngineEvent(event: UsnaPhysicsEngine.EngineEvent) {
    when (event) {
      is UsnaPhysicsEngine.EngineEvent.Jump -> {
        audio.playJump()
      }
      is UsnaPhysicsEngine.EngineEvent.RecordStart -> {
        audio.playRecordStart()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.RecordEnd -> {
        audio.playRecordEnd()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.ReleaseEcho -> {
        audio.playReleaseEcho()
        vibrator.vibrateAction()
      }
      is UsnaPhysicsEngine.EngineEvent.ButtonPressed -> {
        audio.playButtonPressed()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.ButtonReleased -> {
        audio.playButtonReleased()
      }
      is UsnaPhysicsEngine.EngineEvent.DoorOpen -> {
        audio.playDoorOpen()
      }
      is UsnaPhysicsEngine.EngineEvent.DoorClose -> {
        audio.playDoorClose()
      }
      is UsnaPhysicsEngine.EngineEvent.SpikeDeath -> {
        audio.playSpikeDeath()
        vibrator.vibrateHazard()
      }
      is UsnaPhysicsEngine.EngineEvent.EchoDestroyed -> {
        audio.playSpikeDeath()
        vibrator.vibrateClick()
      }
      is UsnaPhysicsEngine.EngineEvent.LevelWon -> {
        audio.playVictory()
        vibrator.vibrateVictory()
        val currentLvl = UsnaLevels.getLevel(_currentLevelId.value)
        val updated = storage.updateLevelCompletion(
          currentLvl.id,
          event.stars,
          event.timeSec,
          event.echoesUsed
        )
        _progress.value = updated
        _victoryState.value = VictoryState(
          level = currentLvl,
          timeSec = event.timeSec,
          stars = event.stars,
          echoesUsed = event.echoesUsed
        )
      }
    }
  }

  fun updateSettings(newSettings: GameSettings) {
    _settings.value = newSettings
    storage.saveSettings(newSettings)
    applySettingsToAudioAndHaptics(newSettings)
  }

  private fun applySettingsToAudioAndHaptics(settings: GameSettings) {
    audio.soundVolume = settings.soundVolume
    audio.musicVolume = settings.musicVolume
    vibrator.isEnabled = settings.vibrationEnabled
  }

  fun navigateTo(newScreen: ScreenState) {
    _screen.value = newScreen
    if (newScreen == ScreenState.GAMEPLAY) {
      startGameLoop()
    } else {
      stopGameLoop()
    }
  }

  fun startLevel(levelId: Int) {
    _currentLevelId.value = levelId
    val level = UsnaLevels.getLevel(levelId)
    initEngine(level)
    _isPaused.value = false
    _victoryState.value = null
    navigateTo(ScreenState.GAMEPLAY)
  }

  fun restartCurrentLevel() {
    engine.resetLevel()
    _isPaused.value = false
    _victoryState.value = null
  }

  fun nextLevel() {
    val nextId = _currentLevelId.value + 1
    if (nextId <= 10) {
      startLevel(nextId)
    } else {
      navigateTo(ScreenState.LEVEL_SELECT)
    }
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

  fun onInputChanged(input: GameInput) {
    currentInput = input
  }

  fun onRecordToggle() {
    if (engine.isRecording) {
      engine.stopRecording()
    } else {
      engine.startRecording()
    }
  }

  fun onReleaseEcho() {
    engine.releaseEcho()
  }

  fun onUndoEcho() {
    if (engine.undoLastEcho()) {
      vibrator.vibrateClick()
    }
  }

  fun setSkinsVisible(visible: Boolean) {
    _showSkins.value = visible
  }

  fun selectSkin(skinId: String) {
    val updated = _settings.value.copy(selectedSkinId = skinId)
    updateSettings(updated)
  }

  private fun startGameLoop() {
    stopGameLoop()
    gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
      val frameDurationMs = 1000L / UsnaPhysicsEngine.TICK_RATE
      while (isActive) {
        val start = System.currentTimeMillis()
        if (!_isPaused.value && _victoryState.value == null) {
          engine.tick(currentInput)
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
