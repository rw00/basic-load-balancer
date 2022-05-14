package com.rw.loadbalancer.provider

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class UuidProvider : Provider {
    private val uuid: String = UUID.randomUUID().toString()
    private val active: AtomicBoolean = AtomicBoolean(true) // TO DO : starts inactive?

    override fun getId(): String = uuid

    override fun isActive(): Boolean {
        return active.get()
    }

    override fun activate(): Boolean {
        return active.compareAndSet(false, true)
    }

    override fun deactivate(): Boolean {
        return active.compareAndSet(true, false)
    }
}
