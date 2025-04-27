"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.WireGuardError = exports.WireGuardStatus = void 0;
/**
 * Possible tunnel states.
 */
var WireGuardStatus;
(function (WireGuardStatus) {
    WireGuardStatus["DISCONNECTED"] = "disconnected";
    WireGuardStatus["CONNECTING"] = "connecting";
    WireGuardStatus["CONNECTED"] = "connected";
    WireGuardStatus["DISCONNECTING"] = "disconnecting";
    WireGuardStatus["ERROR"] = "error";
})(WireGuardStatus || (exports.WireGuardStatus = WireGuardStatus = {}));
/**
 * Errors thrown by the WireGuard module.
 */
class WireGuardError extends Error {
    /** Machine-readable error code. */
    code;
    /** Underlying cause, if any. */
    cause;
    constructor(code, message, cause) {
        super(message);
        this.name = 'WireGuardError';
        this.code = code;
        this.cause = cause;
        // Restore prototype chain
        Object.setPrototypeOf(this, new.target.prototype);
    }
}
exports.WireGuardError = WireGuardError;
//# sourceMappingURL=types.js.map