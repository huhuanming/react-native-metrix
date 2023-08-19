import type { onMetrixUpdateCallback, metrixUpdateInfo } from './NativeMetrix';

export function start() {}

export function stop() {}

export function onUpdate(_: onMetrixUpdateCallback) {}

export function getTimeSinceStartup(): number {
  return 0;
}

export type { metrixUpdateInfo };
