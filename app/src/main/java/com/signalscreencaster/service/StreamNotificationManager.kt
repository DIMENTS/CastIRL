package com.signalscreencaster.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.signalscreencaster.R
import com.signalscreencaster.streaming.ConnectionState
import com.signalscreencaster.ui.MainActivity
import com.signalscreencaster.util.FormatUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "stream_channel"
    }

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Streaming",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Active stream notification" }
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(state: ConnectionState, bitrateBps: Long) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stream)
            .setContentTitle("Signal Screencaster")
            .setContentText(stateText(state, bitrateBps))
            .setOngoing(true)
            .setContentIntent(launchPendingIntent())
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent())
            .build()

    fun update(state: ConnectionState, bitrateBps: Long) {
        manager.notify(NOTIFICATION_ID, buildNotification(state, bitrateBps))
    }

    private fun stateText(state: ConnectionState, bitrateBps: Long) = when (state) {
        is ConnectionState.Connected    -> "Live · ${FormatUtil.formatBitrate(bitrateBps)}"
        is ConnectionState.Connecting   -> "Connecting…"
        is ConnectionState.Error        -> "Error: ${state.reason}"
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Idle         -> "Ready"
    }

    private fun launchPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(context, StreamingService::class.java).apply { action = ACTION_STOP }
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val ACTION_STOP = "com.signalscreencaster.STOP_STREAM"
    }
}
