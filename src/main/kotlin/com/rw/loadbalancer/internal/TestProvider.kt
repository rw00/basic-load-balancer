package com.rw.loadbalancer.internal

import com.rw.loadbalancer.provider.Provider
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

open class TestProvider(private val id: String = UUID.randomUUID().toString()) : Provider {
    private val healthIndicator: AtomicBoolean = AtomicBoolean(true)

    override fun getId(): String {
        return id
    }

    override fun get(): String {
        if (DebugUtil.DEBUG_ENABLED) {
            Thread.sleep(DebugUtil.DEBUG_WAIT_TIME_MILLIS)
        }
        return id
    }

    override fun check(): Boolean {
        return healthIndicator.get()
    }

    fun overrideHealth(health: Boolean) {
        this.healthIndicator.set(health)
    }
}
