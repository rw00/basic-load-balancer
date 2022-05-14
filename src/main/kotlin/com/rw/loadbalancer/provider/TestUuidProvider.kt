package com.rw.loadbalancer.provider

import java.util.concurrent.atomic.AtomicBoolean

class TestUuidProvider : UuidProvider() {
    private val healthIndicator: AtomicBoolean = AtomicBoolean(true)

    override fun check(): Boolean {
        return healthIndicator.get()
    }

    fun overrideHealth(health: Boolean) {
        this.healthIndicator.set(health)
    }
}
