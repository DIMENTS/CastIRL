package com.castIRL.streaming

data class StreamStats(
    val bitrateBps: Long = 0L,
    val fps: Int = 0,
    val droppedFrames: Int = 0,
    val sentFrames: Int = 0,
    val bytesSent: Long = 0L,
    val durationMs: Long = 0L,
    val hasCongestion: Boolean = false
)
