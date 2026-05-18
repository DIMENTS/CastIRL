package com.signalscreencaster.util

import android.media.MediaCodecList
import android.os.Build
import com.signalscreencaster.data.model.VideoCodecPref

object CodecChecker {
    fun availableVideoCodecs(): List<VideoCodecPref> {
        val codecs = mutableListOf(VideoCodecPref.H264)
        val infos  = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos

        if (infos.any { it.isEncoder && it.supportedTypes.any { t -> t.equals("video/hevc", ignoreCase = true) } })
            codecs += VideoCodecPref.H265

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            infos.any { it.isEncoder && it.supportedTypes.any { t -> t.equals("video/av01", ignoreCase = true) } })
            codecs += VideoCodecPref.AV1

        return codecs
    }
}
