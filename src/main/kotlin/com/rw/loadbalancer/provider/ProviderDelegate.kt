package com.rw.loadbalancer.provider

import com.rw.loadbalancer.internal.DebugUtil
import java.util.concurrent.atomic.AtomicBoolean

class ProviderDelegate(private val instance: Provider) : Provider {
    private val active: AtomicBoolean = AtomicBoolean(true) // TO DO : starts inactive?

    override fun getId(): String {
        return instance.getId()
    }

    override fun get(): String {
        if (DebugUtil.DEBUG_ENABLED) {
            Thread.sleep(DebugUtil.DEBUG_WAIT_TIME_MILLIS)
        }
        return instance.getId()
    }

    override fun check(): Boolean {
        return instance.check()
    }

    fun isActive(): Boolean {
        return active.get()
    }

    /**
     * Activates the provider instance to accept calls.
     *
     * Returns true if the switch was flicked meaning the active state changed. Otherwise, false.
     */
    fun activate(): Boolean {
        return active.compareAndSet(false, true)
    }

    /**
     * Deactivates the provider instance to no longer accept requests.
     *
     * Returns true if the switch was flicked meaning the active state changed. Otherwise, false.
     */
    fun deactivate(): Boolean {
        return active.compareAndSet(true, false)
    }
}
