package com.rw.loadbalancer.internal

object DebugUtil {
    val DEBUG_ENABLED: Boolean = System.getProperty("debug")?.toBoolean() == true
    const val DEBUG_WAIT_TIME_MILLIS: Long = 5000
}
