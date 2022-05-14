package com.rw.loadbalancer.internal

import java.time.Duration

object DebugUtil {
    val DEBUG_ENABLED: Boolean = System.getProperty("debug")?.toBoolean() == true
    val DEBUG_WAIT_TIME: Duration = Duration.ofSeconds(5)
}
