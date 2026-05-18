package com.signalscreencaster.util

import com.signalscreencaster.data.model.ConnectionConfig

object SrtUrlBuilder {
    fun build(config: ConnectionConfig): String {
        val params = buildList {
            if (config.srtLatencyUs != 120_000) add("latency=${config.srtLatencyUs}")
            if (config.srtPassphrase.isNotBlank()) {
                add("passphrase=${config.srtPassphrase}")
                add("pbkeylen=${config.srtPbkeylen}")
            }
        }
        val query = if (params.isEmpty()) "" else "?${params.joinToString("&")}"
        return "${config.srtUrl.trimEnd('/')}$query"
    }
}
