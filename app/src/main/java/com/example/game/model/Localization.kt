package com.example.game.model

object UsnaStrings {
  fun get(key: String, lang: String = "pt"): String {
    val isPt = lang.lowercase().startsWith("pt")
    return when (key) {
      "app_title" -> "USNA"
      "subtitle" -> if (isPt) "Puzzle de Plataformas Temporal" else "Time-Loop Puzzle Platformer"
      "play" -> if (isPt) "Jogar" else "Play"
      "levels" -> if (isPt) "Níveis" else "Levels"
      "settings" -> if (isPt) "Definições" else "Settings"
      "how_to_play" -> if (isPt) "Como Jogar" else "How to Play"
      "skins" -> if (isPt) "Skins" else "Skins"
      "select_skin" -> if (isPt) "Vestiário do Eco-Sprite" else "Eco-Sprite Wardrobe"
      "equipped" -> if (isPt) "Equipada" else "Equipped"
      "equip" -> if (isPt) "Equipar" else "Equip"
      "locked" -> if (isPt) "Bloqueada" else "Locked"
      "exit" -> if (isPt) "Sair" else "Exit"
      "back" -> if (isPt) "Voltar" else "Back"
      "resume" -> if (isPt) "Continuar" else "Resume"
      "restart" -> if (isPt) "Reiniciar" else "Restart"
      "undo" -> if (isPt) "DESFAZER" else "UNDO"
      "next_level" -> if (isPt) "Próximo Nível" else "Next Level"
      "select_level" -> if (isPt) "Escolher Nível" else "Select Level"
      "main_menu" -> if (isPt) "Menu Principal" else "Main Menu"
      "level_completed" -> if (isPt) "Nível Concluído!" else "Level Completed!"
      "paused" -> if (isPt) "Jogo Pausado" else "Game Paused"
      "time" -> if (isPt) "Tempo" else "Time"
      "echoes" -> if (isPt) "Ecos" else "Echoes"
      "record" -> if (isPt) "GRAVAR" else "RECORD"
      "stop_record" -> if (isPt) "PARAR" else "STOP"
      "release" -> if (isPt) "LIBERTAR" else "RELEASE"
      "jump" -> if (isPt) "SALTAR" else "JUMP"
      "music_volume" -> if (isPt) "Volume da Música" else "Music Volume"
      "sfx_volume" -> if (isPt) "Volume dos Efeitos" else "SFX Volume"
      "vibration" -> if (isPt) "Vibração Háptica" else "Haptic Vibration"
      "language" -> if (isPt) "Idioma" else "Language"
      "on" -> if (isPt) "Ligado" else "On"
      "off" -> if (isPt) "Desligado" else "Off"
      "total_stars" -> if (isPt) "Total de Estrelas" else "Total Stars"
      "tutorial_goal" -> if (isPt) "Objetivo: Alcança o portal luminoso para concluir cada nível." else "Goal: Reach the luminous portal to clear each stage."
      "tutorial_record" -> if (isPt) "Gravação: Carrega em GRAVAR para registar até 3 segundos de movimento." else "Recording: Press RECORD to store up to 3 seconds of movement."
      "tutorial_release" -> if (isPt) "Libertar Eco: Carrega em LIBERTAR para invocar até 3 ecos simultâneos." else "Release Echo: Press RELEASE to deploy up to 3 concurrent echoes."
      "tutorial_mechanics" -> if (isPt) "Usa os teus ecos para pressionar botões, segurar portas ou ultrapassar espinhos!" else "Use your echoes to trigger switches, hold doors open, or clear spikes!"
      "congratulations" -> if (isPt) "Parabéns! Dominaste todos os 10 níveis do USNA!" else "Congratulations! You mastered all 10 USNA levels!"
      else -> key
    }
  }
}
