/**
 * Configuration for a WireGuard tunnel.
 */
export interface WireGuardConfig {
    /** Base64-encoded private key for the client. */
    privateKey: string;
    /** Base64-encoded public key for the server. */
    publicKey: string;
    /** IP address or hostname of the WireGuard server. */
    serverAddress: string;
    /** UDP port on which the server listens (usually 51820). */
    serverPort: number;
    /** CIDR ranges allowed through the tunnel (e.g. "0.0.0.0/0"). */
    allowedIPs: string[];
    /** DNS servers to use when connected (e.g. ["1.1.1.1"]). */
    dns: string[];
    /** Maximum transmission unit; defaults to 1420 if omitted. */
    mtu?: number;
    /** Optional pre-shared key (Base64) for extra encryption layer. */
    presharedKey?: string;
}
/**
 * Possible tunnel states.
 */
export declare enum WireGuardStatus {
    DISCONNECTED = "disconnected",
    CONNECTING = "connecting",
    CONNECTED = "connected",
    DISCONNECTING = "disconnecting",
    ERROR = "error"
}
/**
 * Errors thrown by the WireGuard module.
 */
export declare class WireGuardError extends Error {
    /** Machine-readable error code. */
    readonly code: string;
    /** Underlying cause, if any. */
    readonly cause?: Error;
    constructor(code: string, message: string, cause?: Error);
}
