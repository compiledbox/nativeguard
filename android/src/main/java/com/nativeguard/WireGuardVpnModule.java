package com.nativeguard;

import com.facebook.react.bridge.*;
import com.facebook.react.module.annotations.ReactModule;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

@ReactModule(name = WireGuardVpnModule.NAME)
public class WireGuardVpnModule extends ReactContextBaseJavaModule {
    public static final String NAME = "WireGuardVpnModule";

    public WireGuardVpnModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Initialize the VPN backend (e.g. permissions, service setup).
     */
    @ReactMethod
    public void initialize(Promise promise) {
        try {
            // TODO: actual initialization logic
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("INITIALIZE_ERROR", "Failed to initialize VPN backend", e);
        }
    }

    /**
     * Start a WireGuard tunnel with the given config.
     */
    @ReactMethod
    public void connect(ReadableMap config, Promise promise) {
        try {
            // Validate required fields
            if (!config.hasKey("privateKey") || config.getString("privateKey") == null) {
                promise.reject("INVALID_CONFIG", "Missing 'privateKey'");
                return;
            }
            if (!config.hasKey("publicKey") || config.getString("publicKey") == null) {
                promise.reject("INVALID_CONFIG", "Missing 'publicKey'");
                return;
            }
            if (!config.hasKey("serverAddress") || config.getString("serverAddress") == null) {
                promise.reject("INVALID_CONFIG", "Missing 'serverAddress'");
                return;
            }
            if (!config.hasKey("serverPort")) {
                promise.reject("INVALID_CONFIG", "Missing 'serverPort'");
                return;
            }

            // Extract fields
            String privateKey    = config.getString("privateKey");
            String publicKey     = config.getString("publicKey");
            String serverAddress = config.getString("serverAddress");
            int    serverPort    = config.getInt("serverPort");

            // allowedIPs
            ReadableArray allowedIPsArray = config.getArray("allowedIPs");
            if (allowedIPsArray == null) {
                promise.reject("INVALID_CONFIG", "Missing 'allowedIPs'");
                return;
            }
            List<String> allowedIPs = new ArrayList<>();
            for (int i = 0; i < allowedIPsArray.size(); i++) {
                allowedIPs.add(allowedIPsArray.getString(i));
            }

            // dns
            ReadableArray dnsArray = config.getArray("dns");
            if (dnsArray == null) {
                promise.reject("INVALID_CONFIG", "Missing 'dns'");
                return;
            }
            List<String> dns = new ArrayList<>();
            for (int i = 0; i < dnsArray.size(); i++) {
                dns.add(dnsArray.getString(i));
            }

            // optional fields
            int    mtu           = config.hasKey("mtu") ? config.getInt("mtu") : 1420;
            String presharedKey  = config.hasKey("presharedKey") 
                                    ? config.getString("presharedKey") 
                                    : null;

            // TODO: use these values to start the WireGuard tunnel
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("CONNECT_ERROR", "Error connecting to VPN: " + e.getMessage(), e);
        }
    }

    /**
     * Tear down the current tunnel.
     */
    @ReactMethod
    public void disconnect(Promise promise) {
        try {
            // TODO: stop the VPN tunnel
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("DISCONNECT_ERROR", "Error disconnecting VPN: " + e.getMessage(), e);
        }
    }

    /**
     * Return one of the WireGuardStatus strings.
     */
    @ReactMethod
    public void getStatus(Promise promise) {
        try {
            // TODO: query real tunnel state
            String status = "disconnected";
            promise.resolve(status);
        } catch (Exception e) {
            promise.reject("STATUS_ERROR", "Error fetching VPN status: " + e.getMessage(), e);
        }
    }

    /**
     * Check whether this device/platform supports WireGuard.
     */
    @ReactMethod
    public void isDeviceSupported(Promise promise) {
        try {
            // TODO: check kernel version or required APIs
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("SUPPORTED_ERROR", "Error checking support: " + e.getMessage(), e);
        }
    }
}
