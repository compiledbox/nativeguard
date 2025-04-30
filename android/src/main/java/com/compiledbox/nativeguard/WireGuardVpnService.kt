package com.compiledbox.nativeguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.TunnelManager

class WireGuardVpnService : VpnService() {

    private lateinit var tunnelManager: TunnelManager
    private var tunnel: Tunnel? = null

    companion object {
        const val CHANNEL_ID = "NativeGuardVpnChannel"
        const val NOTIF_ID = 1

        @Volatile
        var instance: WireGuardVpnService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        tunnelManager = TunnelManager(this, GoBackend())
        createNotificationChannel()
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Bring service into foreground so Android lets the VPN run
        startForeground(NOTIF_ID, buildNotification("VPN Service starting…"))
        return START_STICKY
    }

    /** Starts or updates a tunnel given a name and full WG config text. */
    @Throws(Exception::class)
    fun startTunnel(name: String, configText: String) {
        val cfg = configText.toByteArray()
        tunnel = tunnelManager.getTunnel(name)?.apply {
            setConfiguration(cfg)
        } ?: tunnelManager.createTunnel(name, cfg)
        tunnel!!.up()
        // Update notification
        updateNotification("VPN connected")
    }

    /** Stops the active tunnel. */
    fun stopTunnel() {
        tunnel?.down()
        updateNotification("VPN disconnected")
        stopForeground(true)
        stopSelf()
    }

    /** Returns one of the Tunnel.State values, lowercased. */
    fun getTunnelState(): String =
        tunnel?.state?.name?.lowercase() ?: "disconnected"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID, "NativeGuard VPN", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Notifications for NativeGuard VPN status" }
            (getSystemService(NotificationManager::class.java))
                .createNotificationChannel(chan)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NativeGuard VPN")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_vpn_key) // ensure you have this icon
            .setContentIntent(pending)
            .build()
    }

    private fun updateNotification(text: String) {
        val notif = buildNotification(text)
        (getSystemService(NotificationManager::class.java))
            .notify(NOTIF_ID, notif)
    }
}
