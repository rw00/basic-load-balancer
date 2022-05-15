package com.rw.loadbalancer.provider

import java.util.concurrent.atomic.AtomicBoolean

/**
 * A ProviderDelegate is basically a Provider that can be activated/deactivated.
 */
class ProviderDelegate<T>(private val instance: Provider<T>) : Provider<T> {
    private val active: AtomicBoolean = AtomicBoolean(true) // TO DO : starts inactive?

    override fun getId(): String {
        return instance.getId()
    }

    override fun get(): T {
        return instance.get()
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
