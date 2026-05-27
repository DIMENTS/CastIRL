package com.castIRL.util

import android.media.MediaCodec
import android.media.MediaCodecList
import android.os.Build
import com.castIRL.data.model.VideoCodecPref

object CodecChecker {
    fun availableVideoCodecs(): List<VideoCodecPref> {
        val codecs = mutableListOf(VideoCodecPref.H264)
        val infos  = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos

        if (infos.any { it.isEncoder && it.supportedTypes.any { t -> t.equals("video/hevc", ignoreCase = true) } }
            && canInstantiate("video/hevc"))
            codecs += VideoCodecPref.H265

        // AV1 encoding support via RootEncoder is unreliable across devices —
        // hardware encoder may exist but streaming fails at runtime. Hidden until stable.
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        //     infos.any { it.isEncoder && it.supportedTypes.any { t -> t.equals("video/av01", ignoreCase = true) } }
        //     && canInstantiate("video/av01"))
        //     codecs += VideoCodecPref.AV1

        return codecs
    }

    private fun canInstantiate(mimeType: String): Boolean = try {
        val codec = MediaCodec.createEncoderByType(mimeType)
        val format = android.media.MediaFormat.createVideoFormat(mimeType, 1280, 720).apply {
            setInteger(android.media.MediaFormat.KEY_BIT_RATE, 4_000_000)
            setInteger(android.media.MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(android.media.MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            setInteger(android.media.MediaFormat.KEY_COLOR_FORMAT,
                android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.release()
        true
    } catch (_: Exception) { false }
}
