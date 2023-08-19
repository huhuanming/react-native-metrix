import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface metrixUpdateInfo {
  jsFps: number;
  uiFps: number;
  usedCpu: number;
  usedRam: number;
  viewCount: number;
  visibleViewCount: number;
}

export type onMetrixUpdateCallback = (info: metrixUpdateInfo) => void;

export interface Spec extends TurboModule {
  start(): void;
  stop(): void;
  onUpdate: (callback: onMetrixUpdateCallback) => void;
  getTimeSinceStartup(): number;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Metrix');
