import { useEffect, useRef, useState } from 'react';
import { TrilhoEngine } from './engine';

export function useGameLoop(engine: TrilhoEngine, paused: boolean = false) {
  const [, setTick] = useState(0);
  const pausedRef = useRef(paused);
  pausedRef.current = paused;

  useEffect(() => {
    let animId: number;
    let lastTime = Date.now();
    let accumulator = 0;
    const stepDuration = 1000 / TrilhoEngine.TICK_RATE;

    const loop = () => {
      const now = Date.now();
      const dt = Math.min(now - lastTime, 100);
      lastTime = now;

      if (!pausedRef.current) {
        accumulator += dt;
        while (accumulator >= stepDuration) {
          engine.step();
          accumulator -= stepDuration;
        }
        setTick((t) => (t + 1) % 1000000);
      }

      animId = requestAnimationFrame(loop);
    };

    animId = requestAnimationFrame(loop);

    return () => {
      cancelAnimationFrame(animId);
    };
  }, [engine]);
}
