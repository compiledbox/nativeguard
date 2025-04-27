import type { WireGuardConfig } from './types';
import { WireGuardStatus } from './types';
/**
 * Initialize the VPN engine. Must be called before any other method.
 */
export declare function initialize(): Promise<void>;
/**
 * Establish a WireGuard tunnel using the given configuration.
 */
export declare function connect(config: WireGuardConfig): Promise<void>;
/**
 * Tear down the active WireGuard tunnel.
 */
export declare function disconnect(): Promise<void>;
/**
 * Retrieve the current tunnel status.
 */
export declare function getStatus(): Promise<WireGuardStatus>;
/**
 * Check if the current device/platform supports WireGuard.
 */
export declare function isDeviceSupported(): Promise<boolean>;
export { WireGuardStatus } from './types';
export type { WireGuardConfig } from './types';
