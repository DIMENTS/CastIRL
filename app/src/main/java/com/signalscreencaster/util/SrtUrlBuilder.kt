package com.castIRL.util

import com.castIRL.data.model.ConnectionConfig

object SrtUrlBuilder {
    fun build(config: ConnectionConfig): String {
        val params = buildList {
            if (config.srtStreamId.isNotBlank()) add("streamid=${config.srtStreamId}")
            if (config.srtLatencyMs != 120) add("latency=${config.srtLatencyMs}")
            if (config.srtPassphrase.isNotBlank()) {
                add("passphrase=${config.srtPassphrase}")
                add("pbkeylen=${config.srtPbkeylen}")
            }
        }
        val query = if (params.isEmpty()) "" else "?${params.joinToString("&")}"
        return "${config.srtUrl.trimEnd('/')}$query"
    }
}
