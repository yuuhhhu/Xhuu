package com.mahmoud.vpnpro

import android.net.VpnService
import android.os.ParcelFileDescriptor

/**
 * واجهة محركات الأنفاق. كل بروتوكول يحتاج مكتبة حقيقية:
 *  - OpenVPN  -> ics-openvpn (مكتبة)  - V2Ray -> libv2ray / v2ray-core + tun2socks
 *  - SSH      -> sshj + tun2socks      - WebSocket -> OkHttp WebSocket + tun2socks
 * نفّذ الواجهة وسجّلها هنا. استخدم service.protect(socket) لكل socket خارجي لتجنب حلقة التوجيه.
 */
interface TunnelEngine {
    fun start(service: VpnService, tun: ParcelFileDescriptor, server: Server)
    fun stop()
}

object Engines {
    private val registry = mutableMapOf<String, () -> TunnelEngine>()
    fun register(protocol: String, factory: () -> TunnelEngine) { registry[protocol] = factory }
    fun create(protocol: String): TunnelEngine? = registry[protocol]?.invoke()
}
