export interface Skin {
  id: string;
  namePt: string;
  nameEn: string;
  cost: number;
  color: string;
  secondaryColor: string;
  glowColor: string;
}

export const SKINS: Skin[] = [
  {
    id: 'solaris',
    namePt: 'Corredor Solaris',
    nameEn: 'Solaris Runner',
    cost: 0,
    color: '#FF7A18',
    secondaryColor: '#FFB347',
    glowColor: '#38E1FF',
  },
  {
    id: 'cyber_neon',
    namePt: 'Velocista Neon',
    nameEn: 'Neon Sprinter',
    cost: 150,
    color: '#FF007F',
    secondaryColor: '#FF4081',
    glowColor: '#00E5FF',
  },
  {
    id: 'emerald_tech',
    namePt: 'Expresso Esmeralda',
    nameEn: 'Emerald Express',
    cost: 300,
    color: '#00E676',
    secondaryColor: '#69F0AE',
    glowColor: '#76FF03',
  },
  {
    id: 'phantom',
    namePt: 'Fantasma dos Trilhos',
    nameEn: 'Track Phantom',
    cost: 500,
    color: '#7C4DFF',
    secondaryColor: '#B388FF',
    glowColor: '#EA80FC',
  },
];

export function getSkin(id: string): Skin {
  return SKINS.find((s) => s.id === id) ?? SKINS[0];
}
