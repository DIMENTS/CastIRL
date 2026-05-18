package com.signalscreencaster.util

object FormatUtil {
    fun formatBitrate(bps: Long): String = when {
        bps == 0L          -> "–"
        bps >= 1_000_000L  -> "%.1f Mbps".format(bps / 1_000_000.0)
        else               -> "${bps / 1_000} kbps"
    }

    fun formatDuration(ms: Long): String {
        val s   = ms / 1_000
        val h   = s / 3600
        val m   = (s % 3600) / 60
        val sec = s % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
    }

    fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024L         -> "%.0f KB".format(bytes / 1_024.0)
        else                    -> "$bytes B"
    }
}
