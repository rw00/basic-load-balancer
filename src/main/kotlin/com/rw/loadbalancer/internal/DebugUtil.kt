package com.rw.loadbalancer.internal

object DebugUtil {
    val DEBUG_ENABLED: Boolean = java.lang.Boolean.getBoolean("debug")
    const val DEBUG_WAIT_TIME_MILLIS: Long = 5000
}
