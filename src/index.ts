// src/index.ts

import { NativeModules } from 'react-native';
import { WireGuardError, WireGuardStatus } from './types';
import type { WireGuardConfig } from './types';

interface NativeGuardNativeModule {
  initialize(): Promise<void>;
  connect(config: WireGuardConfig): Promise<void>;
  disconnect(): Promise<void>;
  getStatus(): Promise<string>;
  isDeviceSupported(): Promise<boolean>;
}

const LINKING_ERROR =
  `The native module 'WireGuardVpnModule' for package 'nativeguard' `
  + `is not linked. Make sure you’ve rebuilt your app and, on iOS, run 'pod install'.`;

const NativeGuardModule: NativeGuardNativeModule = 
  NativeModules.WireGuardVpnModule
    ? (NativeModules.WireGuardVpnModule as NativeGuardNativeModule)
    : {
        initialize: () => Promise.reject(new WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        connect: () => Promise.reject(new WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        disconnect: () => Promise.reject(new WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        getStatus: () => Promise.reject(new WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        isDeviceSupported: () => Promise.reject(new WireGuardError('LINKING_ERROR', LINKING_ERROR)),
      };

/**
 * Initialize the VPN engine. Must be called before any other method.
 */
export async function initialize(): Promise<void> {
  try {
    await NativeGuardModule.initialize();
  } catch (err: unknown) {
    if (err instanceof WireGuardError) throw err;
    throw new WireGuardError(
      'INITIALIZATION_FAILED',
      'Failed to initialize VPN service',
      err instanceof Error ? err : undefined
    );
  }
}

/**
 * Establish a WireGuard tunnel using the given configuration.
 */
export async function connect(config: WireGuardConfig): Promise<void> {
  try {
    await NativeGuardModule.connect(config);
  } catch (err: unknown) {
    if (err instanceof WireGuardError) throw err;
    throw new WireGuardError(
      'CONNECT_FAILED',
      `Failed to connect VPN: ${(err as Error).message || err}`,
      err instanceof Error ? err : undefined
    );
  }
}

/**
 * Tear down the active WireGuard tunnel.
 */
export async function disconnect(): Promise<void> {
  try {
    await NativeGuardModule.disconnect();
  } catch (err: unknown) {
    if (err instanceof WireGuardError) throw err;
    throw new WireGuardError(
      'DISCONNECT_FAILED',
      'Failed to disconnect VPN',
      err instanceof Error ? err : undefined
    );
  }
}

/**
 * Retrieve the current tunnel status.
 */
export async function getStatus(): Promise<WireGuardStatus> {
  try {
    const raw = await NativeGuardModule.getStatus();
    if (!Object.values(WireGuardStatus).includes(raw as WireGuardStatus)) {
      throw new WireGuardError(
        'INVALID_STATUS',
        `Received invalid status: "${raw}"`
      );
    }
    return raw as WireGuardStatus;
  } catch (err: unknown) {
    if (err instanceof WireGuardError) throw err;
    throw new WireGuardError(
      'STATUS_FAILED',
      'Failed to retrieve VPN status',
      err instanceof Error ? err : undefined
    );
  }
}

/**
 * Check if the current device/platform supports WireGuard.
 */
export async function isDeviceSupported(): Promise<boolean> {
  try {
    return await NativeGuardModule.isDeviceSupported();
  } catch (err: unknown) {
    if (err instanceof WireGuardError) throw err;
    throw new WireGuardError(
      'UNSUPPORTED_CHECK_FAILED',
      'Failed to check device support',
      err instanceof Error ? err : undefined
    );
  }
}

// Re-export the status enum, config type, and error class
export { WireGuardStatus, WireGuardError };
export type { WireGuardConfig };
