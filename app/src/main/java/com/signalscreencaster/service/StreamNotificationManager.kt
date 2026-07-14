package com.castIRL.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.castIRL.R
import com.castIRL.streaming.ConnectionState
import com.castIRL.streaming.StreamStats
import com.castIRL.ui.MainActivity
import com.castIRL.util.FormatUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP     = "com.castIRL.STOP_STREAM"
        private const val CHANNEL_ID = "stream_channel"
    }

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Samsung One UI gates the AOSP Live Update chip behind its own (hidden) Now Bar
    // pipeline, so the promoted chip doesn't render for third-party apps. On Samsung we
    // use MediaStyle instead, which surfaces reliably in the Now Bar / media area.
    private val isSamsung = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

    private val mediaSession = MediaSessionCompat(context, "CastIRL").apply {
        setCallback(object : MediaSessionCompat.Callback() {
            override fun onStop() {
                context.startService(
                    Intent(context, StreamingService::class.java).apply { action = ACTION_STOP },
                )
            }
        })
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Streaming",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Active stream notification" }
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(state: ConnectionState, stats: StreamStats): android.app.Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stream)
            .setContentTitle(title(state))
            .setContentText(detail(state, stats))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColorized(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(launchPendingIntent())
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent())

        if (Build.VERSION.SDK_INT >= 36 && !isSamsung) {
            // Android 16 Live Update — promoted status-bar chip (Pixel / OEMs that render it).
            // Must be a standard/BigText style (MediaStyle can't be promoted).
            builder
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigDetail(state, stats)))
                .setRequestPromotedOngoing(true)
        } else {
            // Samsung + older devices — MediaStyle for the Now Bar / media-pill look.
            updateMediaSession(state, stats)
            builder.setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0),
            )
        }

        return builder.build()
    }

    fun update(state: ConnectionState, stats: StreamStats) {
        manager.notify(NOTIFICATION_ID, buildNotification(state, stats))
    }

    fun release() {
        mediaSession.isActive = false
        mediaSession.release()
    }

    // --- Media session (drives the Now Bar / media-style pill) ---

    private fun updateMediaSession(state: ConnectionState, stats: StreamStats) {
        val playbackState = when (state) {
            is ConnectionState.Connected  -> PlaybackStateCompat.STATE_PLAYING
            is ConnectionState.Connecting  -> PlaybackStateCompat.STATE_BUFFERING
            else                           -> PlaybackStateCompat.STATE_STOPPED
        }
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title(state))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, detail(state, stats))
                .build(),
        )
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_STOP)
                .setState(playbackState, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build(),
        )
        mediaSession.isActive = state is ConnectionState.Connected || state is ConnectionState.Connecting
    }

    private fun title(state: ConnectionState) = when (state) {
        is ConnectionState.Connected    -> "● LIVE"
        is ConnectionState.Connecting   -> "Connecting…"
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Error        -> "Stream error"
        is ConnectionState.Idle         -> "CastIRL"
    }

    private fun detail(state: ConnectionState, stats: StreamStats) = when (state) {
        is ConnectionState.Connected    ->
            "${FormatUtil.formatBitrate(stats.bitrateBps)} · ${FormatUtil.formatDataUsage(stats.bytesSent)} sent"
        is ConnectionState.Connecting   -> "Starting stream…"
        is ConnectionState.Disconnected -> "Connection lost"
        is ConnectionState.Error        -> state.reason.ifBlank { "Something went wrong" }
        is ConnectionState.Idle         -> "Ready"
    }

    /** Richer multi-line body for the expanded / Live Update view. */
    private fun bigDetail(state: ConnectionState, stats: StreamStats): String = when (state) {
        is ConnectionState.Connected -> buildString {
            append("Bitrate\t${FormatUtil.formatBitrate(stats.bitrateBps)}")
            append("\nData sent\t${FormatUtil.formatDataUsage(stats.bytesSent)}")
            append("\nUptime\t${FormatUtil.formatDuration(stats.durationMs)}")
            if (stats.droppedFrames > 0) append("\nDropped\t${stats.droppedFrames}")
        }
        else -> detail(state, stats)
    }

    private fun launchPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(context, StreamingService::class.java).apply { action = ACTION_STOP }
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
