export type Lane = 'LEFT' | 'CENTER' | 'RIGHT';

export const LANE_X: Record<Lane, number> = {
  LEFT: 144,
  CENTER: 360,
  RIGHT: 576,
};

export type ObstacleType = 
  | 'LOW_BARRIER'    // Jump over
  | 'HIGH_BARRIER'   // Slide under
  | 'TRAIN'          // Tall obstacle occupying lane
  | 'DOUBLE_BLOCK';  // Blocks 2 lanes

export type PickupType = 'COIN' | 'CRYSTAL' | 'SHIELD' | 'MAGNET' | 'BOOST';

export interface Obstacle {
  id: number;
  type: ObstacleType;
  lane: Lane;
  secondLane?: Lane;
  y: number;
  width: number;
  height: number;
  active: boolean;
}

export interface Pickup {
  id: number;
  type: PickupType;
  lane: Lane;
  y: number;
  active: boolean;
}

export interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  color: string;
  size: number;
  life: number;
  maxLife: number;
}

export interface GameProgress {
  totalCoins: number;
  highScoreMeters: number;
  selectedSkinId: string;
  unlockedSkins: string[];
}
