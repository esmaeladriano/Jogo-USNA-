# TRILHO — Endless Runner em React Native + Expo (TypeScript)

Projeto completo do jogo **TRILHO**, um endless runner 2D em 3 faixas com física suave, power-ups, cristais de invulnerabilidade e loja de corredores, construído em **React Native** com **Expo SDK 52** e **TypeScript** em modo estrito (`strict`).

---

## 🎮 Funcionalidades do Jogo

- **Jogabilidade em 3 Faixas**:
  - ↔️ Troca de faixa fluida e ultrarrápida (~0.11s).
  - ⬆️ Salto parabólico para superar barreiras baixas.
  - ⬇️ Deslize rápido para passar por baixo de barreiras altas.
  - 🚆 Desvio de obstáculos de pista dupla e comboios.
- **Poderes e Habilidades**:
  - 💎 **Cristais de Invulnerabilidade**: Apanhe até 3 cristais e prima o botão para ativar 1,5 segundos de escudo supremo.
  - 🧲 **Íman**: Atrai moedas de todas as faixas durante 8 segundos.
  - 🛡️ **Escudo**: Absorve 1 colisão com período de tolerância.
  - ⚡ **Impulso (Boost)**: Velocidade ultra-rápida e invulnerabilidade durante 4 segundos.
- **Ecrã de Fim de Jogo**:
  - Exibição de moedas da corrida e moedas acumuladas totais.
  - Pergunta direta se deseja recomeçar.
  - **Cronómetro de 10 segundos** com barra visual animada para reinício automático.
- **Loja e Personalização**:
  - 4 corredores com cores e auras distintas: *Corredor Solaris*, *Velocista Neon*, *Expresso Esmeralda* e *Fantasma dos Trilhos*.
- **Persistência**:
  - Recorde de distância, total de moedas, corridas e corredores desbloqueados guardados no `@react-native-async-storage/async-storage`.
- **Definições e Internacionalização**:
  - Suporte completo para Português (PT) e Inglês (EN).
  - Toggles para efeitos sonoros, música de fundo e vibração háptica.

---

## 📁 Estrutura de Ficheiros

```text
react_native_trilho/
├── App.tsx                   # Entrada principal e navegação por ecrãs
├── app.json                  # Configuração Expo SDK 52
├── package.json              # Dependências e scripts
├── tsconfig.json             # TypeScript strict
├── babel.config.js           # Preset Babel Expo
├── src/
│   ├── theme.ts              # Paleta de cores e raios de curvatura
│   ├── i18n.ts               # Dicionário bilíngue PT/EN
│   ├── game/
│   │   ├── engine.ts         # Motor puro em TypeScript (60 Hz tick, física, colisões)
│   │   ├── skins.ts          # Definição dos corredores e catálogo
│   │   └── useGameLoop.ts    # Game loop fixo a 60 FPS com acumulador
│   ├── state/
│   │   └── AppContext.tsx    # Estado global e persistência AsyncStorage
│   ├── render/
│   │   └── GameWorld.tsx     # Renderização 2D da pista, corredor, obstáculos e partículas
│   └── screens/
│       ├── MenuScreen.tsx    # Menu inicial, estatísticas e tutorial
│       ├── GameScreen.tsx    # HUD, controlos por swipe e ecrã de game over
│       ├── ShopScreen.tsx    # Loja de corredores com compra e equipamento
│       └── SettingsScreen.tsx# Definições (idioma, som, vibração)
```

---

## 🚀 Como Executar

### 1. Instalar as Dependências
```bash
cd react_native_trilho
npm install
```

### 2. Verificar Tipagem TypeScript
```bash
npm run typecheck
```

### 3. Iniciar o Servidor Expo
```bash
npx expo start
```
- Prima `a` para abrir no emulador Android.
- Prima `i` para abrir no simulador iOS.
- Prima `w` para abrir na versão Web.
- Ou leia o QR Code com a app **Expo Go** no seu telemóvel físico.
