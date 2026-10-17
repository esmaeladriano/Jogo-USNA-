package com.example.game.engine

import com.example.game.model.*

object UsnaLevels {
  fun getAllLevels(): List<LevelData> = listOf(
    createLevel1(),
    createLevel2(),
    createLevel3(),
    createLevel4(),
    createLevel5(),
    createLevel6(),
    createLevel7(),
    createLevel8(),
    createLevel9(),
    createLevel10()
  )

  fun getLevel(id: Int): LevelData {
    return getAllLevels().find { it.id == id } ?: createLevel1()
  }

  // Common boundary walls for 1280x720 arena
  private fun createBorders(): List<Platform> = listOf(
    Platform(Rect2(0f, 0f, 1280f, 40f)),     // Ceiling
    Platform(Rect2(0f, 660f, 1280f, 60f)),   // Floor
    Platform(Rect2(0f, 40f, 40f, 620f)),     // Left wall
    Platform(Rect2(1240f, 40f, 40f, 620f))   // Right wall
  )

  // Nível 1: Primeiros Passos (Andar e Saltar)
  private fun createLevel1(): LevelData {
    val platforms = createBorders() + listOf(
      Platform(Rect2(300f, 540f, 180f, 30f)),
      Platform(Rect2(580f, 440f, 180f, 30f)),
      Platform(Rect2(860f, 340f, 220f, 30f))
    )
    return LevelData(
      id = 1,
      namePt = "1. Primeiros Passos",
      nameEn = "1. First Steps",
      descriptionPt = "Aprende a andar e a saltar para alcançar o portal de saída.",
      descriptionEn = "Learn to run and jump to reach the exit portal.",
      playerSpawn = Vector2(120f, 650f),
      platforms = platforms,
      buttons = emptyList(),
      doors = emptyList(),
      spikes = emptyList(),
      exitPortal = ExitPortal(Rect2(960f, 260f, 60f, 80f)),
      maxEchoes = 0,
      targetTimeSec = 10f
    )
  }

  // Nível 2: O Teu Primeiro Eco (Gravar e Libertar)
  // O jogador grava uma corrida com salto alto. Ao libertar, o eco salta e o jogador pode saltar em cima dele para atingir a plataforma alta!
  private fun createLevel2(): LevelData {
    val platforms = createBorders() + listOf(
      // High barrier dividing room
      Platform(Rect2(540f, 360f, 40f, 300f)),
      Platform(Rect2(580f, 360f, 300f, 30f)),
      Platform(Rect2(980f, 500f, 200f, 30f))
    )
    return LevelData(
      id = 2,
      namePt = "2. O Teu Primeiro Eco",
      nameEn = "2. Your First Echo",
      descriptionPt = "Grava um movimento e liberta um eco para alcançar uma plataforma inacessível.",
      descriptionEn = "Record a movement and release an echo to reach an inaccessible ledge.",
      playerSpawn = Vector2(140f, 650f),
      platforms = platforms,
      buttons = emptyList(),
      doors = emptyList(),
      spikes = emptyList(),
      exitPortal = ExitPortal(Rect2(1060f, 420f, 60f, 80f)),
      maxEchoes = 1,
      echoStaysAtEnd = true,
      targetTimeSec = 15f
    )
  }

  // Nível 3: O Interruptor (Botão de pressão)
  // Um eco tem de ficar em cima do botão enquanto o jogador passa a porta.
  private fun createLevel3(): LevelData {
    val platforms = createBorders() + listOf(
      // Wall with a door in the middle
      Platform(Rect2(700f, 40f, 40f, 400f)),
      Platform(Rect2(700f, 580f, 40f, 80f))
    )
    val buttons = listOf(
      ButtonState(
        id = 1,
        rect = Rect2(360f, 646f, 64f, 14f),
        type = ButtonType.STANDARD
      )
    )
    val doors = listOf(
      DoorState(
        id = 1,
        buttonId = 1,
        rect = Rect2(700f, 440f, 40f, 140f) // Door blocks gap between (440..580)
      )
    )
    return LevelData(
      id = 3,
      namePt = "3. O Interruptor",
      nameEn = "3. The Switch",
      descriptionPt = "Grava o teu eco a pisar o botão para abrir a porta para o teu corpo original.",
      descriptionEn = "Record your echo stepping on the switch to hold the door open for you.",
      playerSpawn = Vector2(140f, 650f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = emptyList(),
      exitPortal = ExitPortal(Rect2(1060f, 580f, 60f, 80f)),
      maxEchoes = 1,
      echoStaysAtEnd = true,
      targetTimeSec = 14f
    )
  }

  // Nível 4: Fosso de Espinhos (Primeiro contacto com espinhos)
  // Um fosso de espinhos perigoso; um botão além dos espinhos ou o eco é enviado além do fosso para abrir a ponte.
  private fun createLevel4(): LevelData {
    val platforms = createBorders() + listOf(
      Platform(Rect2(40f, 500f, 260f, 30f)),  // Start high ledge
      Platform(Rect2(480f, 440f, 120f, 25f)), // Intermediate safe island
      Platform(Rect2(780f, 40f, 40f, 460f)),  // Barrier wall
      Platform(Rect2(780f, 600f, 40f, 60f)),
      Platform(Rect2(900f, 520f, 280f, 30f))  // Goal platform
    )
    val spikes = listOf(
      // Floor spikes across bottom
      SpikeHazard(Rect2(300f, 636f, 480f, 24f), SpikeOrientation.UP)
    )
    val buttons = listOf(
      ButtonState(
        id = 1,
        rect = Rect2(510f, 426f, 60f, 14f),
        type = ButtonType.STANDARD
      )
    )
    val doors = listOf(
      DoorState(
        id = 1,
        buttonId = 1,
        rect = Rect2(780f, 500f, 40f, 100f)
      )
    )
    return LevelData(
      id = 4,
      namePt = "4. Fosso de Espinhos",
      nameEn = "4. Spike Pit",
      descriptionPt = "Cuidado com os espinhos vermelhos! Usa o eco para abrir o caminho com segurança.",
      descriptionEn = "Beware of the red spikes! Use your echo to safely clear the way.",
      playerSpawn = Vector2(120f, 490f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = spikes,
      exitPortal = ExitPortal(Rect2(1040f, 440f, 60f, 80f)),
      maxEchoes = 1,
      echoStaysAtEnd = true,
      targetTimeSec = 16f
    )
  }

  // Nível 5: Sincronia Dupla (Dois botões e dois ecos)
  // Dois botões independentes, ambos precisam de estar ativos (ou desbloquear duas portas consecutivas)
  private fun createLevel5(): LevelData {
    val platforms = createBorders() + listOf(
      // Lower wall with door 1
      Platform(Rect2(600f, 40f, 40f, 340f)),
      Platform(Rect2(600f, 480f, 40f, 180f)),
      // Second wall with door 2
      Platform(Rect2(960f, 40f, 40f, 340f)),
      Platform(Rect2(960f, 480f, 40f, 180f)),
      // Upper platforms
      Platform(Rect2(160f, 460f, 160f, 25f)),
      Platform(Rect2(380f, 340f, 160f, 25f))
    )
    val buttons = listOf(
      ButtonState(id = 1, rect = Rect2(210f, 446f, 60f, 14f)),
      ButtonState(id = 2, rect = Rect2(430f, 326f, 60f, 14f))
    )
    val doors = listOf(
      DoorState(id = 1, buttonId = 1, rect = Rect2(600f, 380f, 40f, 100f)),
      DoorState(id = 2, buttonId = 2, rect = Rect2(960f, 380f, 40f, 100f))
    )
    return LevelData(
      id = 5,
      namePt = "5. Sincronia Dupla",
      nameEn = "5. Dual Synchrony",
      descriptionPt = "Dois botões e dois ecos: coordena a gravação de ambos para abrir as portas sucessivas.",
      descriptionEn = "Two switches and two echoes: coordinate both to pass successive doors.",
      playerSpawn = Vector2(100f, 650f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = emptyList(),
      exitPortal = ExitPortal(Rect2(1100f, 580f, 60f, 80f)),
      maxEchoes = 2,
      echoStaysAtEnd = true,
      targetTimeSec = 20f
    )
  }

  // Nível 6: Mantenha a Porta Aberta
  // Portas verticais que fecham instantaneamente ao soltar. Precisa de sincronização precisa.
  private fun createLevel6(): LevelData {
    val platforms = createBorders() + listOf(
      Platform(Rect2(40f, 460f, 300f, 30f)),
      Platform(Rect2(480f, 40f, 40f, 420f)),
      Platform(Rect2(480f, 560f, 40f, 100f)),
      Platform(Rect2(640f, 420f, 240f, 25f)),
      Platform(Rect2(980f, 40f, 40f, 420f)),
      Platform(Rect2(980f, 560f, 40f, 100f))
    )
    val buttons = listOf(
      ButtonState(id = 1, rect = Rect2(160f, 446f, 64f, 14f)),
      ButtonState(id = 2, rect = Rect2(720f, 406f, 64f, 14f))
    )
    val doors = listOf(
      DoorState(id = 1, buttonId = 1, rect = Rect2(480f, 460f, 40f, 100f)),
      DoorState(id = 2, buttonId = 2, rect = Rect2(980f, 460f, 40f, 100f))
    )
    return LevelData(
      id = 6,
      namePt = "6. Mantenha a Porta Aberta",
      nameEn = "6. Hold The Door",
      descriptionPt = "As portas fecham no segundo em que o botão é largado. Planeia o tempo do teu eco.",
      descriptionEn = "Doors shut the instant the switch is released. Plan your echo's timing carefully.",
      playerSpawn = Vector2(100f, 450f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = emptyList(),
      exitPortal = ExitPortal(Rect2(1100f, 580f, 60f, 80f)),
      maxEchoes = 2,
      echoStaysAtEnd = true,
      targetTimeSec = 22f
    )
  }

  // Nível 7: Contagem Decrescente (Botão temporizado de 2s)
  // O botão dá 2 segundos de janela antes de fechar a porta.
  private fun createLevel7(): LevelData {
    val platforms = createBorders() + listOf(
      Platform(Rect2(120f, 500f, 220f, 25f)),
      Platform(Rect2(480f, 40f, 40f, 460f)),
      Platform(Rect2(480f, 600f, 40f, 60f)),
      Platform(Rect2(620f, 480f, 200f, 25f))
    )
    val buttons = listOf(
      ButtonState(
        id = 1,
        rect = Rect2(200f, 486f, 60f, 14f),
        type = ButtonType.TIMED
      )
    )
    val doors = listOf(
      DoorState(id = 1, buttonId = 1, rect = Rect2(480f, 500f, 40f, 100f))
    )
    val spikes = listOf(
      SpikeHazard(Rect2(360f, 636f, 120f, 24f), SpikeOrientation.UP)
    )
    return LevelData(
      id = 7,
      namePt = "7. Contagem Decrescente",
      nameEn = "7. Countdown",
      descriptionPt = "O botão temporizado concede-te apenas 2 segundos após ser largado. Sê rápido!",
      descriptionEn = "The timed switch grants you only 2 seconds after release. Be swift!",
      playerSpawn = Vector2(100f, 650f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = spikes,
      exitPortal = ExitPortal(Rect2(1040f, 580f, 60f, 80f)),
      maxEchoes = 2,
      echoStaysAtEnd = true,
      targetTimeSec = 18f
    )
  }

  // Nível 8: Trindade (Três ecos em simultâneo)
  // Três botões para abrir três secções sucessivas. Usa o limite máximo de 3 ecos!
  private fun createLevel8(): LevelData {
    val platforms = createBorders() + listOf(
      Platform(Rect2(140f, 520f, 180f, 25f)),
      Platform(Rect2(380f, 420f, 180f, 25f)),
      Platform(Rect2(620f, 320f, 180f, 25f)),
      Platform(Rect2(880f, 40f, 40f, 240f)),
      Platform(Rect2(880f, 380f, 40f, 120f)),
      Platform(Rect2(880f, 600f, 40f, 60f)),
      Platform(Rect2(1040f, 40f, 40f, 380f)),
      Platform(Rect2(1040f, 540f, 40f, 120f))
    )
    val buttons = listOf(
      ButtonState(id = 1, rect = Rect2(200f, 506f, 56f, 14f)),
      ButtonState(id = 2, rect = Rect2(440f, 406f, 56f, 14f)),
      ButtonState(id = 3, rect = Rect2(680f, 306f, 56f, 14f))
    )
    val doors = listOf(
      DoorState(id = 1, buttonId = 1, rect = Rect2(880f, 280f, 40f, 100f)),
      DoorState(id = 2, buttonId = 2, rect = Rect2(880f, 500f, 40f, 100f)),
      DoorState(id = 3, buttonId = 3, rect = Rect2(1040f, 420f, 40f, 120f))
    )
    return LevelData(
      id = 8,
      namePt = "8. Trindade",
      nameEn = "8. Trinity",
      descriptionPt = "Ativa o poder máximo dos três ecos em simultâneo para desativar os três bloqueios.",
      descriptionEn = "Harness the full power of three simultaneous echoes to bypass all three locks.",
      playerSpawn = Vector2(100f, 650f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = emptyList(),
      exitPortal = ExitPortal(Rect2(1140f, 580f, 60f, 80f)),
      maxEchoes = 3,
      echoStaysAtEnd = true,
      targetTimeSec = 25f
    )
  }

  // Nível 9: Armadilha Mortal (Espinhos, portas e botões temporizados)
  private fun createLevel9(): LevelData {
    val platforms = createBorders() + listOf(
      Platform(Rect2(40f, 440f, 220f, 25f)),
      Platform(Rect2(360f, 40f, 40f, 480f)),
      Platform(Rect2(360f, 600f, 40f, 60f)),
      Platform(Rect2(480f, 380f, 180f, 25f)),
      Platform(Rect2(760f, 40f, 40f, 320f)),
      Platform(Rect2(760f, 440f, 40f, 220f)),
      Platform(Rect2(880f, 480f, 200f, 25f))
    )
    val spikes = listOf(
      SpikeHazard(Rect2(260f, 636f, 100f, 24f), SpikeOrientation.UP),
      SpikeHazard(Rect2(400f, 636f, 360f, 24f), SpikeOrientation.UP)
    )
    val buttons = listOf(
      ButtonState(id = 1, rect = Rect2(120f, 426f, 56f, 14f), type = ButtonType.TIMED),
      ButtonState(id = 2, rect = Rect2(540f, 366f, 56f, 14f), type = ButtonType.STANDARD)
    )
    val doors = listOf(
      DoorState(id = 1, buttonId = 1, rect = Rect2(360f, 520f, 40f, 80f)),
      DoorState(id = 2, buttonId = 2, rect = Rect2(760f, 360f, 40f, 80f))
    )
    return LevelData(
      id = 9,
      namePt = "9. Armadilha Mortal",
      nameEn = "9. Deadly Trap",
      descriptionPt = "Espinhos mortais e botões temporizados testam o teu domínio sobre o tempo e espaço.",
      descriptionEn = "Deadly spikes and timed switches test your mastery over time and space.",
      playerSpawn = Vector2(100f, 430f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = spikes,
      exitPortal = ExitPortal(Rect2(1040f, 400f, 60f, 80f)),
      maxEchoes = 3,
      echoStaysAtEnd = true,
      targetTimeSec = 25f
    )
  }

  // Nível 10: O Mestre dos Ecos (Grande Final)
  // Combinação magistral de todas as mecânicas: 3 ecos, saltos precisos, espinhos, portas e botões!
  private fun createLevel10(): LevelData {
    val platforms = createBorders() + listOf(
      // Starting chamber
      Platform(Rect2(40f, 520f, 240f, 25f)),
      Platform(Rect2(280f, 40f, 40f, 460f)),
      Platform(Rect2(280f, 580f, 40f, 80f)),
      // Middle upper chamber
      Platform(Rect2(400f, 380f, 200f, 25f)),
      Platform(Rect2(680f, 40f, 40f, 320f)),
      Platform(Rect2(680f, 440f, 40f, 220f)),
      // High perch
      Platform(Rect2(760f, 300f, 180f, 25f)),
      // Final chamber barrier
      Platform(Rect2(1000f, 40f, 40f, 440f)),
      Platform(Rect2(1000f, 560f, 40f, 100f)),
      Platform(Rect2(1080f, 520f, 160f, 25f))
    )
    val spikes = listOf(
      SpikeHazard(Rect2(320f, 636f, 360f, 24f), SpikeOrientation.UP),
      SpikeHazard(Rect2(720f, 636f, 280f, 24f), SpikeOrientation.UP)
    )
    val buttons = listOf(
      ButtonState(id = 1, rect = Rect2(120f, 506f, 56f, 14f), type = ButtonType.TIMED), // 2s door 1
      ButtonState(id = 2, rect = Rect2(470f, 366f, 56f, 14f), type = ButtonType.STANDARD),
      ButtonState(id = 3, rect = Rect2(820f, 286f, 56f, 14f), type = ButtonType.STANDARD)
    )
    val doors = listOf(
      DoorState(id = 1, buttonId = 1, rect = Rect2(280f, 500f, 40f, 80f)),
      DoorState(id = 2, buttonId = 2, rect = Rect2(680f, 360f, 40f, 80f)),
      DoorState(id = 3, buttonId = 3, rect = Rect2(1000f, 480f, 40f, 80f))
    )
    return LevelData(
      id = 10,
      namePt = "10. O Mestre dos Ecos",
      nameEn = "10. Master of Echoes",
      descriptionPt = "O teste definitivo: orquestra três ecos através de armadilhas, temporizadores e portas seladas.",
      descriptionEn = "The ultimate challenge: orchestrate three echoes across traps, timers, and sealed gates.",
      playerSpawn = Vector2(90f, 510f),
      platforms = platforms,
      buttons = buttons,
      doors = doors,
      spikes = spikes,
      exitPortal = ExitPortal(Rect2(1130f, 440f, 60f, 80f)),
      maxEchoes = 3,
      echoStaysAtEnd = true,
      targetTimeSec = 30f
    )
  }
}
