"use strict";
// src/index.ts
Object.defineProperty(exports, "__esModule", { value: true });
exports.WireGuardStatus = void 0;
exports.initialize = initialize;
exports.connect = connect;
exports.disconnect = disconnect;
exports.getStatus = getStatus;
exports.isDeviceSupported = isDeviceSupported;
const react_native_1 = require("react-native");
const types_1 = require("./types");
const LINKING_ERROR = `The native module 'WireGuardVpnModule' for package 'nativeguard' `
    + `is not linked. Make sure you’ve rebuilt your app and, on iOS, run 'pod install'.`;
const NativeGuardModule = react_native_1.NativeModules.WireGuardVpnModule
    ? react_native_1.NativeModules.WireGuardVpnModule
    : {
        initialize: () => Promise.reject(new types_1.WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        connect: () => Promise.reject(new types_1.WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        disconnect: () => Promise.reject(new types_1.WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        getStatus: () => Promise.reject(new types_1.WireGuardError('LINKING_ERROR', LINKING_ERROR)),
        isDeviceSupported: () => Promise.reject(new types_1.WireGuardError('LINKING_ERROR', LINKING_ERROR)),
    };
/**
 * Initialize the VPN engine. Must be called before any other method.
 */
async function initialize() {
    try {
        await NativeGuardModule.initialize();
    }
    catch (err) {
        if (err instanceof types_1.WireGuardError)
            throw err;
        throw new types_1.WireGuardError('INITIALIZATION_FAILED', 'Failed to initialize VPN service', err instanceof Error ? err : undefined);
    }
}
/**
 * Establish a WireGuard tunnel using the given configuration.
 */
async function connect(config) {
    try {
        await NativeGuardModule.connect(config);
    }
    catch (err) {
        if (err instanceof types_1.WireGuardError)
            throw err;
        throw new types_1.WireGuardError('CONNECT_FAILED', `Failed to connect VPN: ${err.message || err}`, err instanceof Error ? err : undefined);
    }
}
/**
 * Tear down the active WireGuard tunnel.
 */
async function disconnect() {
    try {
        await NativeGuardModule.disconnect();
    }
    catch (err) {
        if (err instanceof types_1.WireGuardError)
            throw err;
        throw new types_1.WireGuardError('DISCONNECT_FAILED', 'Failed to disconnect VPN', err instanceof Error ? err : undefined);
    }
}
/**
 * Retrieve the current tunnel status.
 */
async function getStatus() {
    try {
        const raw = await NativeGuardModule.getStatus();
        if (!Object.values(types_1.WireGuardStatus).includes(raw)) {
            throw new types_1.WireGuardError('INVALID_STATUS', `Received invalid status: "${raw}"`);
        }
        return raw;
    }
    catch (err) {
        if (err instanceof types_1.WireGuardError)
            throw err;
        throw new types_1.WireGuardError('STATUS_FAILED', 'Failed to retrieve VPN status', err instanceof Error ? err : undefined);
    }
}
/**
 * Check if the current device/platform supports WireGuard.
 */
async function isDeviceSupported() {
    try {
        return await NativeGuardModule.isDeviceSupported();
    }
    catch (err) {
        if (err instanceof types_1.WireGuardError)
            throw err;
        throw new types_1.WireGuardError('UNSUPPORTED_CHECK_FAILED', 'Failed to check device support', err instanceof Error ? err : undefined);
    }
}
var types_2 = require("./types");
Object.defineProperty(exports, "WireGuardStatus", { enumerable: true, get: function () { return types_2.WireGuardStatus; } });
//# sourceMappingURL=index.js.map