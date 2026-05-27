package com.castIRL.streaming

sealed interface ConnectionState {
    data object Idle         : ConnectionState
    data object Connecting   : ConnectionState
    data object Connected    : ConnectionState
    data object Disconnected : ConnectionState
    data class  Error(val reason: String) : ConnectionState
}
