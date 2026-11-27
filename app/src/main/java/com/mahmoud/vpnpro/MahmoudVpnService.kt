package com.mahmoud.vpnpro

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class MahmoudVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null
    private var engine: TunnelEngine? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopVpn(); return START_NOT_STICKY }
        val s = Server(intent?.getStringExtra("id") ?: "", "", "", intent?.getStringExtra("name") ?: "",
            intent?.getStringExtra("protocol") ?: "", intent?.getStringExtra("host") ?: "", intent?.getIntExtra("port", 0) ?: 0)
        startVpn(s)
        return START_NOT_STICKY
    }

    private fun startVpn(s: Server) {
        VpnState.error.value = null
        VpnState.status.value = Status.Connecting
        val eng = Engines.create(s.protocol)
        if (eng == null) { fail("محرك ${s.protocol} غير مثبت بعد (راجع TunnelEngine.kt)"); return }
        try {
            tun = Builder().setSession("MAHMOUD VPN PRO").addAddress("10.8.0.2", 32)
                .addRoute("0.0.0.0", 0).addDnsServer("1.1.1.1").setMtu(1400).establish()
                ?: return fail("تعذر إنشاء واجهة VPN")
            eng.start(this, tun!!, s); engine = eng
            VpnState.server.value = s
            VpnState.since.value = System.currentTimeMillis()
            VpnState.status.value = Status.Connected
        } catch (e: Exception) { fail(e.message ?: "خطأ في الاتصال") }
    }

    private fun fail(msg: String) { VpnState.error.value = msg; stopVpn() }

    private fun stopVpn() {
        runCatching { engine?.stop() }; engine = null
        runCatching { tun?.close() }; tun = null
        VpnState.status.value = Status.Disconnected
        stopSelf()
    }

    override fun onRevoke() = stopVpn()
    override fun onDestroy() { stopVpn(); super.onDestroy() }

    companion object {
        const val ACTION_STOP = "stop"
        fun start(ctx: Context, s: Server) = ctx.startService(Intent(ctx, MahmoudVpnService::class.java)
            .putExtra("id", s.id).putExtra("name", s.name).putExtra("protocol", s.protocol)
            .putExtra("host", s.host).putExtra("port", s.port))
        fun stop(ctx: Context) = ctx.startService(Intent(ctx, MahmoudVpnService::class.java).setAction(ACTION_STOP))
    }
}
