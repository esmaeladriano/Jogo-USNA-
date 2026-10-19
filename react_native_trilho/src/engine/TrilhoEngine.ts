import { Lane, LANE_X, Obstacle, ObstacleType, Pickup, PickupType, Particle } from '../types';

export class TrilhoEngine {
  public static readonly TICK_RATE = 60;
  public static readonly BASE_SPEED = 840; // Fast and dynamic
  public static readonly PLAYER_BASE_Y = 1020;
  public static readonly MAX_CRYSTALS = 3;

  public currentLane: Lane = 'CENTER';
  public targetLane: Lane = 'CENTER';
  public playerX: number = LANE_X.CENTER;
  public startLaneX: number = LANE_X.CENTER;
  public laneChangeProgress: number = 1.0;

  public isJumping: boolean = false;
  public jumpProgress: number = 0; // 0 to 1
  public jumpHeightOffset: number = 0;

  public isSliding: boolean = false;
  public slideProgress: number = 0;

  public crystalsStock: number = 0;
  public crystalInvulnRemaining: number = 0;

  public hasShield: boolean = false;
  public magnetRemaining: number = 0;
  public boostRemaining: number = 0;

  public distanceMeters: number = 0;
  public distanceSubMeters: number = 0;
  public coinsEarned: number = 0;
  public currentSpeed: number = TrilhoEngine.BASE_SPEED;

  public obstacles: Obstacle[] = [];
  public pickups: Pickup[] = [];
  public particles: Particle[] = [];

  public isGameOver: boolean = false;
  private nextId: number = 1;
  private nextSpawnY: number = -200;

  private onEvent?: (event: string, payload?: any) => void;

  constructor(onEvent?: (event: string, payload?: any) => void) {
    this.onEvent = onEvent;
  }

  public reset() {
    this.currentLane = 'CENTER';
    this.targetLane = 'CENTER';
    this.playerX = LANE_X.CENTER;
    this.startLaneX = LANE_X.CENTER;
    this.laneChangeProgress = 1.0;

    this.isJumping = false;
    this.jumpProgress = 0;
    this.jumpHeightOffset = 0;

    this.isSliding = false;
    this.slideProgress = 0;

    this.crystalsStock = 0;
    this.crystalInvulnRemaining = 0;
    this.hasShield = false;
    this.magnetRemaining = 0;
    this.boostRemaining = 0;

    this.distanceMeters = 0;
    this.distanceSubMeters = 0;
    this.coinsEarned = 0;
    this.currentSpeed = TrilhoEngine.BASE_SPEED;

    this.obstacles = [];
    this.pickups = [];
    this.particles = [];
    this.isGameOver = false;
    this.nextSpawnY = -200;
  }

  public moveLeft() {
    if (this.isGameOver) return;
    if (this.targetLane === 'RIGHT') this.changeLane('CENTER');
    else if (this.targetLane === 'CENTER') this.changeLane('LEFT');
  }

  public moveRight() {
    if (this.isGameOver) return;
    if (this.targetLane === 'LEFT') this.changeLane('CENTER');
    else if (this.targetLane === 'CENTER') this.changeLane('RIGHT');
  }

  private changeLane(lane: Lane) {
    this.startLaneX = this.playerX;
    this.targetLane = lane;
    this.laneChangeProgress = 0;
    this.onEvent?.('LANE_SWITCH');
  }

  public jump() {
    if (this.isGameOver || this.isJumping) return;
    this.isJumping = true;
    this.jumpProgress = 0;
    this.isSliding = false;
    this.onEvent?.('JUMP');
  }

  public slide() {
    if (this.isGameOver) return;
    this.isSliding = true;
    this.slideProgress = 0;
    if (this.isJumping) {
      // Fast drop from jump
      this.isJumping = false;
      this.jumpHeightOffset = 0;
    }
    this.onEvent?.('SLIDE');
  }

  public useCrystal() {
    if (this.isGameOver || this.crystalsStock <= 0 || this.crystalInvulnRemaining > 0) return;
    this.crystalsStock--;
    this.crystalInvulnRemaining = 90; // 1.5s at 60fps
    this.onEvent?.('CRYSTAL_ABILITY');
  }

  public tick() {
    if (this.isGameOver) return;

    // 1. Speed & Distance
    const speedMult = this.boostRemaining > 0 ? 1.85 : 1.0;
    const stage = Math.floor(this.distanceMeters / 500);
    this.currentSpeed = (TrilhoEngine.BASE_SPEED + stage * 50) * speedMult;

    const deltaY = (this.currentSpeed / TrilhoEngine.TICK_RATE);
    this.distanceSubMeters += deltaY * 0.035;
    if (this.distanceSubMeters >= 1.0) {
      const added = Math.floor(this.distanceSubMeters);
      this.distanceMeters += added;
      this.distanceSubMeters -= added;
    }

    // 2. Timers
    if (this.crystalInvulnRemaining > 0) this.crystalInvulnRemaining--;
    if (this.boostRemaining > 0) this.boostRemaining--;
    if (this.magnetRemaining > 0) this.magnetRemaining--;

    // 3. Lane Tween
    if (this.laneChangeProgress < 1.0) {
      this.laneChangeProgress += 0.15; // fast 7-tick switch
      if (this.laneChangeProgress >= 1.0) {
        this.laneChangeProgress = 1.0;
        this.currentLane = this.targetLane;
        this.playerX = LANE_X[this.targetLane];
      } else {
        const targetX = LANE_X[this.targetLane];
        this.playerX = this.startLaneX + (targetX - this.startLaneX) * this.laneChangeProgress;
      }
    }

    // 4. Jump & Slide physics
    if (this.isJumping) {
      this.jumpProgress += 1 / 34;
      if (this.jumpProgress >= 1.0) {
        this.isJumping = false;
        this.jumpProgress = 0;
        this.jumpHeightOffset = 0;
      } else {
        this.jumpHeightOffset = Math.sin(this.jumpProgress * Math.PI) * 150;
      }
    }

    if (this.isSliding) {
      this.slideProgress += 1 / 32;
      if (this.slideProgress >= 1.0) {
        this.isSliding = false;
        this.slideProgress = 0;
      }
    }

    // 5. Move entities downward
    for (const obs of this.obstacles) {
      obs.y += deltaY;
    }
    for (const p of this.pickups) {
      p.y += deltaY;
      // Magnet attraction
      if (this.magnetRemaining > 0 && p.active && p.type === 'COIN') {
        const dx = this.playerX - LANE_X[p.lane];
        const dy = TrilhoEngine.PLAYER_BASE_Y - p.y;
        const dist = Math.hypot(dx, dy);
        if (dist < 450) {
          p.y += dy * 0.15;
        }
      }
    }

    // 6. Collisions & Pickups
    this.checkCollisions();

    // 7. Procedural Spawning
    this.spawnProceduralTrack();

    // 8. Cleanup offscreen
    this.obstacles = this.obstacles.filter(o => o.y < 1400);
    this.pickups = this.pickups.filter(p => p.y < 1400);

    // 9. Particles
    for (const pt of this.particles) {
      pt.x += pt.vx;
      pt.y += pt.vy;
      pt.life--;
    }
    this.particles = this.particles.filter(pt => pt.life > 0);
  }

  private checkCollisions() {
    const playerY = TrilhoEngine.PLAYER_BASE_Y;

    // Pickups
    for (const pick of this.pickups) {
      if (!pick.active) continue;
      const pickX = LANE_X[pick.lane];
      if (Math.abs(this.playerX - pickX) < 55 && Math.abs(playerY - pick.y) < 65) {
        pick.active = false;
        this.collectPickup(pick);
      }
    }

    // Obstacles
    const isInvulnerable = this.crystalInvulnRemaining > 0 || this.boostRemaining > 0;
    for (const obs of this.obstacles) {
      if (!obs.active) continue;
      const obsX = LANE_X[obs.lane];
      const laneMatch = Math.abs(this.playerX - obsX) < 55 ||
        (obs.secondLane && Math.abs(this.playerX - LANE_X[obs.secondLane]) < 55);

      if (laneMatch && Math.abs(playerY - obs.y) < (obs.height / 2 + 35)) {
        if (obs.type === 'LOW_BARRIER' && this.jumpHeightOffset > 75) {
          continue; // Successfully jumped over
        }
        if (obs.type === 'HIGH_BARRIER' && this.isSliding) {
          continue; // Successfully slid under
        }

        // Hit!
        if (isInvulnerable) {
          obs.active = false;
          this.spawnHitParticles(obsX, obs.y, '#38E1FF');
          continue;
        }

        if (this.hasShield) {
          this.hasShield = false;
          obs.active = false;
          this.spawnHitParticles(obsX, obs.y, '#FFD700');
          this.onEvent?.('SHIELD_BROKEN');
          continue;
        }

        // Fatal crash
        this.isGameOver = true;
        this.onEvent?.('GAME_OVER', { distance: this.distanceMeters, coins: this.coinsEarned });
        break;
      }
    }
  }

  private collectPickup(pickup: Pickup) {
    if (pickup.type === 'COIN') {
      this.coinsEarned++;
      this.spawnHitParticles(this.playerX, TrilhoEngine.PLAYER_BASE_Y, '#FFD700');
      this.onEvent?.('COIN');
    } else if (pickup.type === 'CRYSTAL') {
      if (this.crystalsStock < TrilhoEngine.MAX_CRYSTALS) this.crystalsStock++;
      this.spawnHitParticles(this.playerX, TrilhoEngine.PLAYER_BASE_Y, '#38E1FF');
      this.onEvent?.('CRYSTAL');
    } else if (pickup.type === 'SHIELD') {
      this.hasShield = true;
      this.onEvent?.('SHIELD_COLLECTED');
    } else if (pickup.type === 'MAGNET') {
      this.magnetRemaining = 480; // 8s
      this.onEvent?.('MAGNET_COLLECTED');
    } else if (pickup.type === 'BOOST') {
      this.boostRemaining = 240; // 4s
      this.onEvent?.('BOOST_COLLECTED');
    }
  }

  private spawnProceduralTrack() {
    const highestEntityY = Math.min(
      ...this.obstacles.map(o => o.y),
      ...this.pickups.map(p => p.y),
      this.nextSpawnY
    );

    if (highestEntityY > -300) {
      this.nextSpawnY = highestEntityY - 320;
      this.generateSafePattern(this.nextSpawnY);
    }
  }

  private generateSafePattern(spawnY: number) {
    const lanes: Lane[] = ['LEFT', 'CENTER', 'RIGHT'];
    // Guarantee at least one free exit lane
    const safeLane = lanes[Math.floor(Math.random() * lanes.length)];
    const dangerLanes = lanes.filter(l => l !== safeLane);

    const types: ObstacleType[] = ['LOW_BARRIER', 'HIGH_BARRIER', 'TRAIN', 'DOUBLE_BLOCK'];
    const chosenType = types[Math.floor(Math.random() * types.length)];

    if (chosenType === 'DOUBLE_BLOCK') {
      this.obstacles.push({
        id: this.nextId++,
        type: 'DOUBLE_BLOCK',
        lane: dangerLanes[0],
        secondLane: dangerLanes[1],
        y: spawnY,
        width: 320,
        height: 70,
        active: true
      });
    } else {
      for (const dl of dangerLanes) {
        this.obstacles.push({
          id: this.nextId++,
          type: chosenType,
          lane: dl,
          y: spawnY,
          width: 140,
          height: chosenType === 'TRAIN' ? 140 : 60,
          active: true
        });
      }
    }

    // Spawn coins on safe lane
    this.pickups.push({
      id: this.nextId++,
      type: Math.random() < 0.1 ? 'CRYSTAL' : (Math.random() < 0.15 ? 'SHIELD' : 'COIN'),
      lane: safeLane,
      y: spawnY,
      active: true
    });
  }

  private spawnHitParticles(x: number, y: number, color: string) {
    for (let i = 0; i < 8; i++) {
      const angle = (Math.PI * 2 * i) / 8;
      this.particles.push({
        x,
        y,
        vx: Math.cos(angle) * (3 + Math.random() * 4),
        vy: Math.sin(angle) * (3 + Math.random() * 4),
        color,
        size: 5 + Math.random() * 4,
        life: 18,
        maxLife: 18
      });
    }
  }
}
