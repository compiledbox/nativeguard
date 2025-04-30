package com.compiledbox.nativeguard

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = WireGuardVpnModule.NAME)
class WireGuardVpnModule(private val ctx: ReactApplicationContext) :
    ReactContextBaseJavaModule(ctx), ActivityEventListener {

    companion object {
        const val NAME = "WireGuardVpnModule"
        private const val PREPARE_VPN = 0xABCD
    }

    private var initPromise: Promise? = null

    init {
        ctx.addActivityEventListener(this)
    }

    override fun getName(): String = NAME

    /** Request user permission for VPN. */
    @ReactMethod
    fun initialize(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("INITIALIZE_ERROR", "No current activity")
            return
        }
        val intent = VpnService.prepare(ctx)
        if (intent != null) {
            initPromise = promise
            activity.startActivityForResult(intent, PREPARE_VPN)
        } else {
            // Already granted
            promise.resolve(null)
        }
    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == PREPARE_VPN) {
            initPromise?.apply {
                if (resultCode == Activity.RESULT_OK) resolve(null)
                else reject("INITIALIZE_ERROR", "VPN permission denied")
            }
            initPromise = null
        }
    }

    override fun onNewIntent(intent: Intent?) = Unit

    /** Build config, start the service, and bring up the tunnel. */
    @ReactMethod
    fun connect(configMap: ReadableMap, promise: Promise) {
        try {
            val configText = buildConfigText(configMap)
            // Start the service in foreground
            val intent = Intent(ctx, WireGuardVpnService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= 26)
                ctx.startForegroundService(intent)
            else
                ctx.startService(intent)
            // Give the service a moment to initialize
            Thread.sleep(200)
            WireGuardVpnService.instance?.startTunnel("nativeguard", configText)
                ?: throw IllegalStateException("VPN service not running")
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("CONNECT_ERROR", "Error connecting VPN: ${e.message}", e)
        }
    }

    /** Stop the service’s tunnel. */
    @ReactMethod
    fun disconnect(promise: Promise) {
        try {
            WireGuardVpnService.instance?.stopTunnel()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("DISCONNECT_ERROR", "Error disconnecting VPN: ${e.message}", e)
        }
    }

    /** Query current state from the service. */
    @ReactMethod
    fun getStatus(promise: Promise) {
        try {
            val status = WireGuardVpnService.instance?.getTunnelState() ?: "disconnected"
            promise.resolve(status)
        } catch (e: Exception) {
            promise.reject("STATUS_ERROR", "Error getting status: ${e.message}", e)
        }
    }

    /** Check API level for VPNService support. */
    @ReactMethod
    fun isDeviceSupported(promise: Promise) {
        promise.resolve(android.os.Build.VERSION.SDK_INT >= 21)
    }

    /** Render a full WireGuard .conf from the JS-provided map. */
    private fun buildConfigText(cfg: ReadableMap): String {
        val priv = cfg.getString("privateKey")!!
        val pub = cfg.getString("publicKey")!!
        val addr = cfg.getString("serverAddress")!!
        val port = cfg.getInt("serverPort")
        val allowed = cfg.getArray("allowedIPs")!!.toArrayList().joinToString(",")
        val dns    = cfg.getArray("dns")!!.toArrayList().joinToString(",")
        val mtu    = if (cfg.hasKey("mtu")) cfg.getInt("mtu") else 1420
        val ps     = cfg.getString("presharedKey")

        return buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = $priv")
            appendLine("MTU = $mtu")
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = $pub")
            appendLine("Endpoint = $addr:$port")
            appendLine("AllowedIPs = $allowed")
            appendLine("DNS = $dns")
            ps?.let { appendLine("PresharedKey = $it") }
        }
    }
}
