package com.rw.loadbalancer.provider

interface Provider<T> : Identifiable {
    /**
     * A simplified operation on the node.
     *
     * Currently, it only returns the ID
     */
    fun get(): T

    /**
     * Supposedly a remote call. Analogous to pinging.
     *
     * Returns true if in good health.
     */
    fun check(): Boolean
}
