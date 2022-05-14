package com.rw.loadbalancer.provider

interface Provider {
    fun getId(): String

    /**
     * A simplified operation on the node.
     *
     * Currently, it only returns the ID
     */
    fun get(): String

    /**
     * Supposedly a remote call. Analogous to pinging.
     *
     * Returns true if in good health.
     */
    fun check(): Boolean
}
