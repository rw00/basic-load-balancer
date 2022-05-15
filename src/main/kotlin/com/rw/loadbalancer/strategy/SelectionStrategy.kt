package com.rw.loadbalancer.strategy

import com.rw.loadbalancer.provider.ProviderDelegate

interface SelectionStrategy {
    /**
     * Returns an active Provider from the Registry based on some selection algorithm.
     *
     * In theory, this Provider might be down and not functioning properly but still not discovered to be ill.
     */
    fun <T> next(): ProviderDelegate<T>?
}
