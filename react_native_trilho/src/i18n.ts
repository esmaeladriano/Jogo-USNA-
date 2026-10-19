export type Language = 'pt' | 'en';

const pt = {
  tagline: 'Corrida eterna nos trilhos',
  play: 'Jogar',
  shop: 'Loja',
  settings: 'Definições',
  best: 'Recorde',
  runs: 'Corridas',
  coins: 'Moedas',
  runCoins: 'Moedas desta corrida',
  totalCoins: 'Moedas acumuladas',
  resume: 'Continuar',
  restart: 'Reiniciar',
  menu: 'Menu',
  paused: 'Jogo pausado',
  gameOver: 'Fim da linha!',
  newRecord: 'NOVO RECORDE!',
  playAgain: 'Jogar outra vez',
  autoRestart: 'Reinicia automaticamente em {s}s',
  shield: 'Escudo',
  buy: 'Comprar',
  equip: 'Equipar',
  equipped: 'Equipado',
  notEnough: 'Moedas insuficientes',
  vibration: 'Vibração',
  language: 'Idioma',
  howToPlay: 'Como jogar',
  tut1: 'Desliza para a esquerda ou direita para mudar de faixa.',
  tut2: 'Desliza para cima para saltar barreiras baixas.',
  tut3: 'Desliza para baixo para passar por baixo das barreiras altas.',
  tut4: 'Apanha cristais (máx. 3) e toca no 💎 para 1,5 s de invulnerabilidade.',
  tut5: 'Power-ups: 🧲 íman de moedas, 🛡 escudo e ⚡ impulso.',
  back: 'Voltar',
  shopTitle: 'Corredores',
  settingsTitle: 'Definições',
} as const;

export type TKey = keyof typeof pt;

const en: Record<TKey, string> = {
  tagline: 'Endless run on the tracks',
  play: 'Play',
  shop: 'Shop',
  settings: 'Settings',
  best: 'Best',
  runs: 'Runs',
  coins: 'Coins',
  runCoins: 'Coins this run',
  totalCoins: 'Total coins',
  resume: 'Resume',
  restart: 'Restart',
  menu: 'Menu',
  paused: 'Game paused',
  gameOver: 'End of the line!',
  newRecord: 'NEW RECORD!',
  playAgain: 'Play again',
  autoRestart: 'Restarts automatically in {s}s',
  shield: 'Shield',
  buy: 'Buy',
  equip: 'Equip',
  equipped: 'Equipped',
  notEnough: 'Not enough coins',
  vibration: 'Vibration',
  language: 'Language',
  howToPlay: 'How to play',
  tut1: 'Swipe left or right to change lanes.',
  tut2: 'Swipe up to jump over low barriers.',
  tut3: 'Swipe down to slide under high barriers.',
  tut4: 'Collect crystals (max 3) and tap 💎 for 1.5s invulnerability.',
  tut5: 'Power-ups: 🧲 coin magnet, 🛡 shield and ⚡ speed boost.',
  back: 'Back',
  shopTitle: 'Runners',
  settingsTitle: 'Settings',
};

export function t(lang: Language, key: TKey, params?: Record<string, string | number>): string {
  const dict = lang === 'en' ? en : pt;
  let text: string = dict[key] ?? pt[key] ?? key;
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      text = text.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v));
    });
  }
  return text;
}
