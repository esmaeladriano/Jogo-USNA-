export type Lane = 'LEFT' | 'CENTER' | 'RIGHT';

export const LANE_VALUES: Lane[] = ['LEFT', 'CENTER', 'RIGHT'];

export const LANE_X: Record<Lane, number> = {
  LEFT: 190,
  CENTER: 360,
  RIGHT: 530,
};

export type ObstacleType = 'BARRIER_LOW' | 'BARRIER_HIGH' | 'TRAIN' | 'DOUBLE_BARRIER';
export type ObstacleHeight = 'LOW' | 'HIGH' | 'FULL';

export type PickupType = 'COIN' | 'CRYSTAL' | 'POWERUP_MAGNET' | 'POWERUP_SHIELD' | 'POWERUP_BOOST';

export type PowerUpType = 'MAGNET' | 'SHIELD' | 'BOOST';

export interface TrackObstacle {
  id: number;
  type: ObstacleType;
  lane: Lane;
  y: number;
  width: number;
  height: number;
  active: boolean;
}

export interface TrackPickup {
  id: number;
  type: PickupType;
  lane: Lane;
  y: number;
  x: number;
  active: boolean;
}

export interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  life: number;
  maxLife: number;
  color: string;
  size: number;
}

export type EngineEvent =
  | { type: 'lane' }
  | { type: 'jump' }
  | { type: 'slide' }
  | { type: 'coin' }
  | { type: 'crystal' }
  | { type: 'powerup'; powerUp: PowerUpType }
  | { type: 'crystal_ability' }
  | { type: 'shield_absorbed' }
  | { type: 'crash' }
  | { type: 'game_over'; distance: number; coins: number };

export class TrilhoEngine {
  public static readonly TICK_RATE = 60;
  public static readonly DELTA_TIME = 1 / 60;
  public static readonly SCREEN_WIDTH = 720;
  public static readonly SCREEN_HEIGHT = 1280;
  public static readonly PLAYER_BASE_Y = 1020;
  public static readonly BASE_SPEED = 840;
  public static readonly SPEED_STEP_PER_500M = 50;
  public static readonly MAX_CRYSTALS = 3;
  public static readonly JUMP_DURATION_TICKS = 34;
  public static readonly SLIDE_DURATION_TICKS = 32;
  public static readonly LANE_CHANGE_DURATION_TICKS = 7;
  public static readonly CRYSTAL_INVULN_DURATION_TICKS = 90;

  // Player State
  public currentLane: Lane = 'CENTER';
  public targetLane: Lane = 'CENTER';
  public playerX: number = LANE_X.CENTER;
  public startLaneX: number = LANE_X.CENTER;
  public laneChangeTicksRemaining: number = 0;

  public isJumping: boolean = false;
  public jumpTicks: number = 0;
  public jumpHeightOffset: number = 0;

  public isSliding: boolean = false;
  public slideTicks: number = 0;

  public crystalsStock: number = 0;
  public crystalInvulnTicks: number = 0;

  // Power-ups
  public hasShield: boolean = false;
  public magnetTicksRemaining: number = 0;
  public boostTicksRemaining: number = 0;
  public graceInvulnTicks: number = 0;

  // Metrics
  public distancePx: number = 0;
  public runCoins: number = 0;
  public isGameOver: boolean = false;
  public currentSpeed: number = TrilhoEngine.BASE_SPEED;

  public get distanceMeters(): number {
    return Math.floor(this.distancePx / 12);
  }

  // Entities
  public activeObstacles: TrackObstacle[] = [];
  public activePickups: TrackPickup[] = [];
  public particles: Particle[] = [];

  private nextEntityId: number = 1;
  private nextSpawnY: number = -200;
  public trackSleeperOffset: number = 0;

  public onEvent?: (event: EngineEvent) => void;

  constructor(onEvent?: (event: EngineEvent) => void) {
    this.onEvent = onEvent;
    this.reset();
  }

  public reset(): void {
    this.currentLane = 'CENTER';
    this.targetLane = 'CENTER';
    this.playerX = LANE_X.CENTER;
    this.startLaneX = LANE_X.CENTER;
    this.laneChangeTicksRemaining = 0;

    this.isJumping = false;
    this.jumpTicks = 0;
    this.jumpHeightOffset = 0;

    this.isSliding = false;
    this.slideTicks = 0;

    this.crystalsStock = 0;
    this.crystalInvulnTicks = 0;

    this.hasShield = false;
    this.magnetTicksRemaining = 0;
    this.boostTicksRemaining = 0;
    this.graceInvulnTicks = 0;

    this.distancePx = 0;
    this.runCoins = 0;
    this.isGameOver = false;
    this.currentSpeed = TrilhoEngine.BASE_SPEED;

    this.activeObstacles = [];
    this.activePickups = [];
    this.particles = [];
    this.nextSpawnY = -200;
    this.trackSleeperOffset = 0;

    this.generateInitialTrack();
  }

  public moveLeft(): boolean {
    if (this.isGameOver) return false;
    const curIdx = LANE_VALUES.indexOf(this.targetLane);
    if (curIdx > 0) {
      this.changeLane(LANE_VALUES[curIdx - 1]);
      return true;
    }
    return false;
  }

  public moveRight(): boolean {
    if (this.isGameOver) return false;
    const curIdx = LANE_VALUES.indexOf(this.targetLane);
    if (curIdx < LANE_VALUES.length - 1) {
      this.changeLane(LANE_VALUES[curIdx + 1]);
      return true;
    }
    return false;
  }

  private changeLane(newLane: Lane): void {
    this.targetLane = newLane;
    this.startLaneX = this.playerX;
    this.laneChangeTicksRemaining = TrilhoEngine.LANE_CHANGE_DURATION_TICKS;
    this.onEvent?.({ type: 'lane' });
  }

  public jump(): boolean {
    if (this.isGameOver || this.isJumping) return false;
    this.isSliding = false;
    this.slideTicks = 0;

    this.isJumping = true;
    this.jumpTicks = TrilhoEngine.JUMP_DURATION_TICKS;
    this.spawnParticleBurst(this.playerX, TrilhoEngine.PLAYER_BASE_Y + 10, 10, '#38E1FF');
    this.onEvent?.({ type: 'jump' });
    return true;
  }

  public slide(): boolean {
    if (this.isGameOver) return false;
    if (this.isJumping) {
      this.isJumping = false;
      this.jumpTicks = 0;
      this.jumpHeightOffset = 0;
    }
    this.isSliding = true;
    this.slideTicks = TrilhoEngine.SLIDE_DURATION_TICKS;
    this.spawnParticleBurst(this.playerX, TrilhoEngine.PLAYER_BASE_Y + 20, 8, '#FF9E40');
    this.onEvent?.({ type: 'slide' });
    return true;
  }

  public useCrystalAbility(): boolean {
    if (this.isGameOver || this.crystalsStock <= 0 || this.crystalInvulnTicks > 0) return false;
    this.crystalsStock--;
    this.crystalInvulnTicks = TrilhoEngine.CRYSTAL_INVULN_DURATION_TICKS;
    this.spawnParticleBurst(this.playerX, TrilhoEngine.PLAYER_BASE_Y - 20, 25, '#38E1FF', 1.8);
    this.onEvent?.({ type: 'crystal_ability' });
    return true;
  }

  public step(): void {
    if (this.isGameOver) return;

    // 1. Calculate dynamic speed
    const speedMultiplier = this.boostTicksRemaining > 0 ? 1.85 : 1.0;
    const stage = Math.floor(this.distanceMeters / 500);
    this.currentSpeed = (TrilhoEngine.BASE_SPEED + stage * TrilhoEngine.SPEED_STEP_PER_500M) * speedMultiplier;

    const moveDelta = this.currentSpeed * TrilhoEngine.DELTA_TIME;
    this.distancePx += moveDelta;
    this.trackSleeperOffset = (this.trackSleeperOffset + moveDelta) % 90;

    // 2. Lane change interpolation
    if (this.laneChangeTicksRemaining > 0) {
      this.laneChangeTicksRemaining--;
      const progress = 1 - this.laneChangeTicksRemaining / TrilhoEngine.LANE_CHANGE_DURATION_TICKS;
      const eased = 0.5 - 0.5 * Math.cos(progress * Math.PI);
      const targetX = LANE_X[this.targetLane];
      this.playerX = this.startLaneX + (targetX - this.startLaneX) * eased;
      if (this.laneChangeTicksRemaining === 0) {
        this.currentLane = this.targetLane;
        this.playerX = targetX;
      }
    }

    // 3. Jump parabolic arc
    if (this.isJumping) {
      this.jumpTicks--;
      const progress = 1 - this.jumpTicks / TrilhoEngine.JUMP_DURATION_TICKS;
      this.jumpHeightOffset = 130 * 4 * progress * (1 - progress);
      if (this.jumpTicks <= 0) {
        this.isJumping = false;
        this.jumpHeightOffset = 0;
        this.spawnParticleBurst(this.playerX, TrilhoEngine.PLAYER_BASE_Y + 10, 10, '#38E1FF');
      }
    }

    // 4. Slide duration
    if (this.isSliding) {
      this.slideTicks--;
      if (this.slideTicks <= 0) {
        this.isSliding = false;
      }
    }

    // 5. Timers
    if (this.crystalInvulnTicks > 0) this.crystalInvulnTicks--;
    if (this.graceInvulnTicks > 0) this.graceInvulnTicks--;
    if (this.magnetTicksRemaining > 0) this.magnetTicksRemaining--;
    if (this.boostTicksRemaining > 0) this.boostTicksRemaining--;

    // 6. Move objects
    this.updateObstacles(moveDelta);
    this.updatePickups(moveDelta);

    // 7. Collisions
    this.checkCollisions();

    // 8. Procedural track
    this.generateProceduralTrack(moveDelta);

    // 9. Particles
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.life -= TrilhoEngine.DELTA_TIME;
      if (p.life <= 0) {
        this.particles.splice(i, 1);
      } else {
        p.x += p.vx * TrilhoEngine.DELTA_TIME;
        p.y += p.vy * TrilhoEngine.DELTA_TIME;
      }
    }
  }

  private updateObstacles(moveDelta: number): void {
    for (let i = this.activeObstacles.length - 1; i >= 0; i--) {
      const obs = this.activeObstacles[i];
      obs.y += moveDelta;
      if (obs.y > TrilhoEngine.SCREEN_HEIGHT + 200) {
        this.activeObstacles.splice(i, 1);
      }
    }
  }

  private updatePickups(moveDelta: number): void {
    const isMagnet = this.magnetTicksRemaining > 0 || this.boostTicksRemaining > 0;
    for (let i = this.activePickups.length - 1; i >= 0; i--) {
      const item = this.activePickups[i];
      item.y += moveDelta;

      if (isMagnet && item.type === 'COIN' && item.y >= 300 && item.y <= TrilhoEngine.PLAYER_BASE_Y + 100) {
        const dx = this.playerX - item.x;
        const dy = TrilhoEngine.PLAYER_BASE_Y - 20 - item.y;
        item.x += dx * 0.22;
        item.y += dy * 0.18;
      }

      if (item.y > TrilhoEngine.SCREEN_HEIGHT + 100) {
        this.activePickups.splice(i, 1);
      }
    }
  }

  private checkCollisions(): void {
    const isInvulnerable = this.crystalInvulnTicks > 0 || this.boostTicksRemaining > 0 || this.graceInvulnTicks > 0;

    // Pickups
    for (let i = this.activePickups.length - 1; i >= 0; i--) {
      const item = this.activePickups[i];
      if (!item.active) continue;

      const dx = Math.abs(this.playerX - item.x);
      const dy = Math.abs(TrilhoEngine.PLAYER_BASE_Y - this.jumpHeightOffset - item.y);

      if (dx < 60 && dy < 65) {
        item.active = false;
        this.activePickups.splice(i, 1);

        switch (item.type) {
          case 'COIN':
            this.runCoins++;
            this.spawnParticleBurst(item.x, item.y, 6, '#FFD54A');
            this.onEvent?.({ type: 'coin' });
            break;
          case 'CRYSTAL':
            if (this.crystalsStock < TrilhoEngine.MAX_CRYSTALS) {
              this.crystalsStock++;
            }
            this.spawnParticleBurst(item.x, item.y, 14, '#38E1FF', 1.4);
            this.onEvent?.({ type: 'crystal' });
            break;
          case 'POWERUP_MAGNET':
            this.magnetTicksRemaining = Math.floor(8.0 * TrilhoEngine.TICK_RATE);
            this.spawnParticleBurst(item.x, item.y, 16, '#00E5FF');
            this.onEvent?.({ type: 'powerup', powerUp: 'MAGNET' });
            break;
          case 'POWERUP_SHIELD':
            this.hasShield = true;
            this.spawnParticleBurst(item.x, item.y, 16, '#22E38B');
            this.onEvent?.({ type: 'powerup', powerUp: 'SHIELD' });
            break;
          case 'POWERUP_BOOST':
            this.boostTicksRemaining = Math.floor(4.0 * TrilhoEngine.TICK_RATE);
            this.spawnParticleBurst(item.x, item.y, 22, '#FF4D5E', 1.6);
            this.onEvent?.({ type: 'powerup', powerUp: 'BOOST' });
            break;
        }
      }
    }

    // Obstacles
    for (const obs of this.activeObstacles) {
      if (!obs.active) continue;

      const inLane =
        obs.type === 'DOUBLE_BARRIER'
          ? this.currentLane === obs.lane ||
            (obs.lane === 'LEFT' && this.currentLane === 'CENTER') ||
            (obs.lane === 'RIGHT' && this.currentLane === 'CENTER') ||
            (obs.lane === 'CENTER' && (this.currentLane === 'LEFT' || this.currentLane === 'RIGHT'))
          : obs.lane === this.currentLane;

      if (!inLane) continue;

      const playerTop = TrilhoEngine.PLAYER_BASE_Y - (this.isSliding ? 28 : 64);
      const playerBottom = TrilhoEngine.PLAYER_BASE_Y;
      const obsTop = obs.y - obs.height;
      const obsBottom = obs.y;

      const yOverlap = playerTop < obsBottom && playerBottom > obsTop;

      if (yOverlap) {
        let bypassed = false;
        if (obs.type === 'BARRIER_LOW') {
          bypassed = this.jumpHeightOffset > 45;
        } else if (obs.type === 'BARRIER_HIGH') {
          bypassed = this.isSliding;
        }

        if (!bypassed) {
          if (isInvulnerable) {
            obs.active = false;
            this.spawnParticleBurst(LANE_X[obs.lane], obs.y, 18, '#FF4D5E', 1.5);
          } else if (this.hasShield) {
            this.hasShield = false;
            obs.active = false;
            this.graceInvulnTicks = 60;
            this.spawnParticleBurst(this.playerX, TrilhoEngine.PLAYER_BASE_Y - 30, 20, '#22E38B');
            this.onEvent?.({ type: 'shield_absorbed' });
          } else {
            this.isGameOver = true;
            this.spawnParticleBurst(this.playerX, TrilhoEngine.PLAYER_BASE_Y - 20, 30, '#FF4D5E', 2.0);
            this.onEvent?.({ type: 'crash' });
            this.onEvent?.({
              type: 'game_over',
              distance: this.distanceMeters,
              coins: this.runCoins,
            });
            break;
          }
        }
      }
    }
  }

  private generateInitialTrack(): void {
    this.nextSpawnY = -300;
    for (let i = 0; i < 5; i++) {
      this.spawnTrackSegment(this.nextSpawnY);
      this.nextSpawnY -= 480;
    }
  }

  private generateProceduralTrack(moveDelta: number): void {
    this.nextSpawnY += moveDelta;
    if (this.nextSpawnY > -300) {
      this.spawnTrackSegment(-800);
      this.nextSpawnY = -800;
    }
  }

  private spawnTrackSegment(spawnY: number): void {
    const pattern = Math.floor(Math.random() * 10);
    switch (pattern) {
      case 0:
        this.spawnCoinRun('CENTER', spawnY - 60, 4);
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'BARRIER_LOW',
          lane: 'CENTER',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        break;
      case 1:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'TRAIN',
          lane: 'LEFT',
          y: spawnY,
          width: 120,
          height: 180,
          active: true,
        });
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'BARRIER_HIGH',
          lane: 'CENTER',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.spawnCoinRun('RIGHT', spawnY, 4);
        break;
      case 2:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'DOUBLE_BARRIER',
          lane: 'LEFT',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.activePickups.push({
          id: this.nextEntityId++,
          type: 'CRYSTAL',
          lane: 'RIGHT',
          y: spawnY,
          x: LANE_X.RIGHT,
          active: true,
        });
        break;
      case 3:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'DOUBLE_BARRIER',
          lane: 'RIGHT',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.activePickups.push({
          id: this.nextEntityId++,
          type: 'POWERUP_MAGNET',
          lane: 'LEFT',
          y: spawnY,
          x: LANE_X.LEFT,
          active: true,
        });
        break;
      case 4:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'TRAIN',
          lane: 'CENTER',
          y: spawnY,
          width: 120,
          height: 180,
          active: true,
        });
        this.spawnCoinRun('LEFT', spawnY, 3);
        this.spawnCoinRun('RIGHT', spawnY, 3);
        break;
      case 5:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'BARRIER_LOW',
          lane: 'LEFT',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'BARRIER_LOW',
          lane: 'RIGHT',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.activePickups.push({
          id: this.nextEntityId++,
          type: 'POWERUP_BOOST',
          lane: 'CENTER',
          y: spawnY,
          x: LANE_X.CENTER,
          active: true,
        });
        break;
      case 6:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'BARRIER_HIGH',
          lane: 'LEFT',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'BARRIER_HIGH',
          lane: 'CENTER',
          y: spawnY,
          width: 120,
          height: 60,
          active: true,
        });
        this.activePickups.push({
          id: this.nextEntityId++,
          type: 'POWERUP_SHIELD',
          lane: 'RIGHT',
          y: spawnY,
          x: LANE_X.RIGHT,
          active: true,
        });
        break;
      case 7:
        this.spawnCoinRun('LEFT', spawnY, 5);
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'TRAIN',
          lane: 'RIGHT',
          y: spawnY,
          width: 120,
          height: 180,
          active: true,
        });
        break;
      case 8:
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'TRAIN',
          lane: 'LEFT',
          y: spawnY,
          width: 120,
          height: 180,
          active: true,
        });
        this.activeObstacles.push({
          id: this.nextEntityId++,
          type: 'TRAIN',
          lane: 'RIGHT',
          y: spawnY,
          width: 120,
          height: 180,
          active: true,
        });
        this.spawnCoinRun('CENTER', spawnY, 4);
        break;
      default:
        this.spawnCoinRun('CENTER', spawnY, 6);
        break;
    }
  }

  private spawnCoinRun(lane: Lane, startY: number, count: number): void {
    const x = LANE_X[lane];
    for (let i = 0; i < count; i++) {
      this.activePickups.push({
        id: this.nextEntityId++,
        type: 'COIN',
        lane,
        y: startY - i * 65,
        x,
        active: true,
      });
    }
  }

  private spawnParticleBurst(x: number, y: number, count: number, color: string, speedScale: number = 1.0): void {
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = (60 + Math.random() * 200) * speedScale;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        life: 0.35 + Math.random() * 0.3,
        maxLife: 0.65,
        color,
        size: 3 + Math.random() * 4,
      });
    }
  }
}
