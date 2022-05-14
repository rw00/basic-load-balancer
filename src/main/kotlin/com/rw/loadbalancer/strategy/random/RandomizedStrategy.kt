package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.RegistryAwareStrategy
import com.rw.loadbalancer.provider.ProviderDelegate

class RandomizedStrategy : RegistryAwareStrategy {
    private val randomizedMap: RandomizedMap<ProviderDelegate> = RandomizedMap()

    override fun next(): ProviderDelegate {
        return randomizedMap.random
    }

    override fun added(provider: ProviderDelegate) {
        randomizedMap.insert(provider)
    }

    override fun removed(provider: ProviderDelegate) {
        randomizedMap.remove(provider)
    }
}
