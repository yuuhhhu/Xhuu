package com.mahmoud.vpnpro

import kotlinx.coroutines.flow.MutableStateFlow

enum class Status { Disconnected, Connecting, Connected }

object VpnState {
    val status = MutableStateFlow(Status.Disconnected)
    val server = MutableStateFlow<Server?>(null)
    val since = MutableStateFlow(0L)
    val error = MutableStateFlow<String?>(null)
}
