# USNA — Jogo de Puzzle de Plataformas 2D
## Guia Completo de Desenvolvimento, Testes e Exportação (Android APK & iOS)

---

## 1. Visão Geral do Projeto
O **USNA** é um jogo de plataformas e lógica temporal onde o jogador controla o *Eco-Sprite* e grava pequenos fragmentos de movimento (até 3 segundos) para invocar até 3 ecos simultâneos. Os ecos repetem de forma determinística os movimentos frame a frame para pressionar botões, segurar portas e ultrapassar perigos.

### Especificações Técnicas
- **Motor:** Godot 4.3 (GDScript 2D) / Android Nativo (Jetpack Compose 60Hz Physics Canvas)
- **Resolução Base:** 1280x720 (Paisagem / Landscape)
- **Stretch Mode:** `canvas_items` com aspect `expand`
- **Renderizador:** Mobile (otimizado para smartphones de gama média e alta)
- **Controlos:** Multitoque virtual (Thumb layout com margens seguras via `DisplayServer.get_display_safe_area()`) e teclado de teste (WASD / Setas, Espaço, R, G, L).

---

## 2. Estrutura de Ficheiros do Projeto Godot
```
godot_project/
├── project.godot               # Configurações do motor (Landscape 1280x720, inputs, autoloads)
├── export_presets.cfg          # Presets oficiais configurados para Android APK e iOS
├── icon.svg                    # Ícone vetorial do jogo
├── data/
│   └── levels.json             # Definição geométrica dos 10 níveis completos
├── scenes/
│   ├── Main.tscn               # Cena principal de orquestração
│   ├── MainMenu.tscn           # Menu principal (Jogar, Níveis, Definições)
│   ├── Player.tscn             # Cena do jogador com colisões e física
│   ├── Echo.tscn               # Cena do Eco semitransparente determinístico
│   ├── Button.tscn             # Botão de pressão (Standard e Temporizado)
│   ├── Door.tscn               # Porta laser deslizante com colisão dinâmica
│   ├── Spike.tscn              # Obstáculo mortal de espinhos
│   └── HUD.tscn                # Interface tátil com safe area
├── scripts/
│   ├── Player.gd               # Movimento, coyote time (0.1s), jump buffer (0.1s), gravação 60Hz
│   ├── Echo.gd                 # Reprodução determinística frame a frame
│   ├── Button.gd               # Lógica de pressão e temporizador de 2s
│   ├── Door.gd                 # Abertura e fecho suave ligado a botões
│   ├── Spike.gd                # Deteção de colisão mortal
│   ├── LevelManager.gd         # Gestão de ecos (até 3), reinício e vitória
│   ├── GameData.gd             # Gravação e leitura local (user://usna_save.json)
│   ├── AudioManager.gd         # Sons e feedback tátil (vibração)
│   └── VirtualControls.gd      # Controlos virtuais adaptáveis à área segura
└── README_GUIA_EXPORTACAO.md   # Este guia
```

---

## 3. Os 10 Níveis Implementados
1. **Nível 1 (Primeiros Passos):** Movimentação e salto simples entre plataformas até ao portal de saída.
2. **Nível 2 (O Teu Primeiro Eco):** Salto alto onde o eco serve de impulso ou indicador temporal para alcançar a plataforma alta.
3. **Nível 3 (O Interruptor):** Um eco mantém o botão de pressão verde ativo enquanto o jogador atravessa a porta.
4. **Nível 4 (Fosso de Espinhos):** Espinhos vermelhos bloqueiam o caminho; o eco é usado para acionar o botão do outro lado em segurança.
5. **Nível 5 (Sincronia Dupla):** Dois botões independentes abrem duas portas consecutivas, exigindo a coordenação de 2 ecos.
6. **Nível 6 (Mantenha a Porta Aberta):** Portas sem atraso que fecham no instante em que o botão é largado.
7. **Nível 7 (Contagem Decrescente):** Primeiro botão temporizado (2 segundos de tolerância). O jogador deve correr contra o tempo.
8. **Nível 8 (Trindade):** Utilização da capacidade máxima de 3 ecos em simultâneo para destravar três trincos de segurança.
9. **Nível 9 (Armadilha Mortal):** Labirinto de espinhos com botões temporizados e portas duplas.
10. **Nível 10 (O Mestre dos Ecos):** Grande final combinando 3 ecos, botões temporizados, armadilhas e saltos de precisão milimétrica.

---

## 4. Guia Passo a Passo: Exportar para Android (APK)

### Pré-requisitos
1. Godot 4.x instalado.
2. Android SDK instalado (Command-line Tools, Build-Tools e Platform 34).
3. OpenJDK 17.

### Passo 1: Configurar o Godot para Android
1. No Godot, vai a **Editor > Definições do Editor (Editor Settings)**.
2. Na categoria **Export > Android**, define:
   - `Android SDK Path`: caminho para a tua pasta do SDK (ex: `C:/Users/user/AppData/Local/Android/Sdk` ou `/Users/user/Library/Android/sdk`).
   - `Debug Keystore`: caminho para o `debug.keystore`.

### Passo 2: Gerar Keystore de Release (para produção)
Num terminal, corre:
```bash
keytool -v -genkeypair -v -keystore usna-release.keystore -alias usna -keyalg RSA -keysize 2048 -validity 10000
```
Guarda a palavra-passe e o ficheiro `.keystore` num local seguro.

### Passo 3: Exportar o APK
1. No Godot, abre o menu **Projeto > Exportar (Project > Export)**.
2. Seleciona o preset **Android** já incluído no ficheiro `export_presets.cfg`.
3. Para exportação de depuração (debug), clica em **Exportar Projeto (Export Project)** com a opção "Exportar com Depuração" ativada.
4. Para exportação final (release assinada):
   - Preenche os campos `Keystore de Release`, `Alias de Release` e `Palavra-passe de Release`.
   - Clica em **Exportar Projeto** e escolhe o ficheiro de destino (ex: `USNA.apk`).

### Passo 4: Instalar no Telemóvel Android
1. No telemóvel, vai a **Definições > Segurança > Instalar aplicações desconhecidas** e ativa para o navegador ou gestor de ficheiros.
2. Transfere o ficheiro `USNA.apk` para o telemóvel (ou corre `adb install USNA.apk` com depuração USB ligada).
3. Abre a aplicação e joga!

---

## 5. Guia Passo a Passo: Exportar para iOS

### Pré-requisitos
- Um computador Mac com macOS atualizado.
- Xcode instalado através da Mac App Store.
- Conta de Programador Apple (Apple Developer Program) ativa.

### Passo 1: Exportar o Projeto Xcode a partir do Godot
1. No Godot (no Mac), abre **Projeto > Exportar**.
2. Seleciona o preset **iOS** configurado em `export_presets.cfg`.
3. Clica em **Exportar Projeto** e escolhe uma pasta vazia de saída (ex: `builds/ios`).
4. O Godot gerará um ficheiro `.xcodeproj` completo com todos os assets e código compilado.

### Passo 2: Abrir no Xcode e Assinar
1. Abre o ficheiro `.xcodeproj` gerado no Xcode.
2. Na barra lateral esquerda, seleciona o projeto raiz do topo (**USNA**).
3. Vai ao separador **Signing & Capabilities**:
   - Marca a caixa **"Automatically manage signing"**.
   - Em **Team**, seleciona a tua equipa da Apple Developer.
   - O `Bundle Identifier` já estará configurado como `com.exemplo.usna` (podes alterar para o identificador registado na tua conta).

### Passo 3: Testar no Dispositivo Físico iPhone / iPad
1. Liga o iPhone ou iPad via cabo USB ao Mac.
2. No menu superior de alvos do Xcode, seleciona o teu dispositivo físico.
3. Clica no botão **Play / Run (⌘R)**.
4. O Xcode compilará, assinará e instalará o jogo diretamente no dispositivo.

### Passo 4: Submeter para TestFlight ou App Store
1. Seleciona o destino **Any iOS Device (arm64)** no topo do Xcode.
2. Vai a **Product > Archive**.
3. Quando a janela *Organizer* abrir, seleciona o arquivo e clica em **Distribute App**.
4. Escolhe **App Store Connect** e segue os passos automáticos de upload para o TestFlight!

---

## 6. Checklist de Testes e Validação de Aceitação

- [x] **Movimento:** Andar, aceleração suave, saltos com altura variável (soltar botão corta o salto a -140px/s).
- [x] **Tolerâncias:** Coyote time de 0,1s e Jump Buffer de 0,1s funcionam de forma impercetível e suave.
- [x] **Gravação:** Botão Gravar regista inputs a 60Hz exatos até ao limite de 3 segundos (180 ticks).
- [x] **Eco:** Ao carregar em Libertar, o eco surge no ponto inicial e reproduz a sequência de forma idêntica e determinística.
- [x] **Limite de Ecos:** Máximo de 3 ecos em simultâneo (respeitando o limite por nível).
- [x] **Botões e Portas:** Botões normais ativos sob peso de corpos; botão temporizado com contagem decrescente de 2 segundos.
- [x] **Espinhos:** Matam o jogador instantaneamente com reinício após 0,5s; destroem ecos no impacto.
- [x] **Níveis:** Todos os 10 níveis possuem solução testada e desbloqueio progressivo de estrelas.
- [x] **Persistência:** Ficheiro local `user://` retém progresso, estrelas e configurações de volume e idioma entre sessões.
- [x] **Controlos Táteis:** Botões virtuais com multitoque, layout ergonómico e compatibilidade com áreas seguras (notch/gestos).
