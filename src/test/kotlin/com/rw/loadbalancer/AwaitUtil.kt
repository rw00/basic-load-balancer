package com.rw.loadbalancer

import org.awaitility.Awaitility
import org.awaitility.core.ThrowingRunnable
import java.time.Duration

fun defaultAwait(
    pollInterval: Duration = Duration.ofMillis(10),
    atMost: Duration = Duration.ofSeconds(2),
    assertion: ThrowingRunnable
) {
    Awaitility.await()
        .pollInterval(pollInterval)
        .atMost(atMost)
        .untilAsserted(assertion)
}
