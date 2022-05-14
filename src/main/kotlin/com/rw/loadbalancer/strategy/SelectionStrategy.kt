package com.rw.loadbalancer.strategy

import com.rw.loadbalancer.provider.ProviderDelegate

interface SelectionStrategy {
    fun next(): ProviderDelegate
}
