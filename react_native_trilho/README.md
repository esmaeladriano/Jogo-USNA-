# TRILHO — Endless Runner (React Native & Expo)

Projeto completo do jogo **TRILHO** convertido para **React Native / TypeScript** com Expo.

## 🚀 Como Executar

### 1. Criar ou clonar o projeto
Para rodar diretamente no seu computador ou telemóvel:

```bash
# Se tiver o Expo CLI:
npx create-expo-app TrilhoRunner --template blank-typescript
cd TrilhoRunner

# Copie os ficheiros da pasta react_native_trilho para dentro do projeto TrilhoRunner:
cp -r /react_native_trilho/* .

# Instalar dependências
npm install
```

### 2. Iniciar o jogo
```bash
npx expo start
```
- Prima **`a`** para abrir no emulador Android
- Prima **`i`** para abrir no simulador iOS
- Prima **`w`** para testar no browser
- Ou leia o QR Code com a app **Expo Go** no seu telemóvel real (Android ou iPhone)

---

## 🎮 Mecânicas Implementadas
1. **Corrida Rápida e Dinâmica**: Velocidade base otimizada (`BASE_SPEED = 840`) com incremento gradual a cada 500m.
2. **3 Faixas Fluidas**: Troca de faixas imediata com interpolação suave.
3. **Salto e Deslize**: Salto físico e deslize rápido (com cancelamento de salto no ar).
4. **Obstáculos Variados**: Barreira baixa (saltar), barreira alta (deslizar), comboio (mudar de faixa) e bloco duplo.
5. **Geração Procedural Segura**: Garante sempre pelo menos uma faixa livre e jogável.
6. **Moedas e Cristais**: Cristais acumuláveis (até 3) para escudo temporário de 1.5s com o botão dedicado no canto inferior direito.
7. **Power-ups**: Íman de moedas (8s), Escudo contra embate e Impulso de velocidade (4s).
8. **Ecrã de Fim de Jogo**:
   - Exibe moedas ganhas na corrida e total acumulado.
   - Pergunta se deseja recomeçar ("Recomeçar?").
   - **Cronómetro de 10s**: Recomeça a corrida automaticamente após 10 segundos se o jogador não tocar em nada!
   - Design limpo com poucos textos e ícones visuais.
