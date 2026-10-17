package com.example.trilho.model

object TrilhoStrings {
  fun get(key: String, lang: String = "pt"): String {
    val isPt = lang.lowercase().startsWith("pt")
    return when (key) {
      "app_name" -> "TRILHO"
      "subtitle" -> if (isPt) "Corrida Eterna nos Trilhos" else "Endless Railway Runner"
      "play" -> if (isPt) "Jogar" else "Play"
      "shop" -> if (isPt) "Loja" else "Shop"
      "records" -> if (isPt) "Recordes" else "Records"
      "settings" -> if (isPt) "Definições" else "Settings"
      "how_to_play" -> if (isPt) "Como Jogar" else "How to Play"
      "exit" -> if (isPt) "Sair" else "Exit"
      "back" -> if (isPt) "Voltar" else "Back"
      "resume" -> if (isPt) "Continuar" else "Resume"
      "restart" -> if (isPt) "Reiniciar" else "Restart"
      "main_menu" -> if (isPt) "Menu Principal" else "Main Menu"
      "game_over" -> if (isPt) "Fim da Corrida!" else "Run Finished!"
      "new_record" -> if (isPt) "NOVO RECORDE!" else "NEW RECORD!"
      "play_again" -> if (isPt) "Jogar Outra Vez" else "Play Again"
      "distance" -> if (isPt) "Distância" else "Distance"
      "coins" -> if (isPt) "Moedas" else "Coins"
      "best_distance" -> if (isPt) "Melhor Distância" else "Best Distance"
      "total_runs" -> if (isPt) "Corridas Totais" else "Total Runs"
      "total_coins" -> if (isPt) "Moedas Acumuladas" else "Total Coins"
      "crystal_ability" -> if (isPt) "ESCUDO DE CRISTAL" else "CRYSTAL SHIELD"
      "crystal_hint" -> if (isPt) "Invulnerabilidade 1.5s" else "1.5s Invulnerability"
      "powerup_magnet" -> if (isPt) "ÍMAME" else "MAGNET"
      "powerup_shield" -> if (isPt) "ESCUDO" else "SHIELD"
      "powerup_boost" -> if (isPt) "IMPULSO" else "BOOST"
      "music_volume" -> if (isPt) "Volume da Música" else "Music Volume"
      "sfx_volume" -> if (isPt) "Volume dos Efeitos" else "SFX Volume"
      "vibration" -> if (isPt) "Vibração Háptica" else "Haptic Vibration"
      "language" -> if (isPt) "Idioma" else "Language"
      "buy" -> if (isPt) "Comprar" else "Buy"
      "equip" -> if (isPt) "Equipar" else "Equip"
      "equipped" -> if (isPt) "Equipado" else "Equipped"
      "not_enough_coins" -> if (isPt) "Moedas insuficientes!" else "Not enough coins!"
      "paused" -> if (isPt) "Jogo Pausado" else "Game Paused"
      "tutorial_swipe_lr" -> if (isPt) "Desliza para a Esquerda/Direita para trocar de faixa." else "Swipe Left/Right to change lanes."
      "tutorial_swipe_up" -> if (isPt) "Desliza para Cima para saltar sobre barreiras baixas." else "Swipe Up to jump over low barriers."
      "tutorial_swipe_down" -> if (isPt) "Desliza para Baixo para deslizar sob barreiras altas." else "Swipe Down to slide under high barriers."
      "tutorial_crystal" -> if (isPt) "Apanha cristais raros (máx 3) e toca no botão para ativar invulnerabilidade temporária!" else "Collect rare crystals (max 3) and tap the button for temporary invulnerability!"
      "ad_reward_note" -> if (isPt) "[Ponto de integração: Ver anúncio para ressuscitar]" else "[Integration hook: Watch ad to revive]"
      else -> key
    }
  }
}
