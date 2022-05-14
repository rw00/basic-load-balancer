package com.rw.loadbalancer.provider

interface Provider {
    fun getId(): String

    fun isActive(): Boolean

    /**
     * Activates the provider to receive requests.
     * Returns true if the switch was flicked. If the active state didn't change, returns false.
     */
    fun activate(): Boolean

    /**
     * Deactivates the provider to no longer accept requests.
     * Returns true if the switch was flicked. If the active state didn't change, returns false.
     */
    fun deactivate(): Boolean
}
