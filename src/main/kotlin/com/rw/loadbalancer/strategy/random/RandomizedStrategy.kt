package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.RegistryAwareStrategy
import com.rw.loadbalancer.provider.Provider

class RandomizedStrategy : RegistryAwareStrategy {
    private val randomizedMap: RandomizedMap<Provider> = RandomizedMap()

    override fun registered(provider: Provider) {
        randomizedMap.insert(provider)
    }

    override fun next(): Provider {
        return randomizedMap.random
    }

    override fun unregistered(provider: Provider) {
        randomizedMap.remove(provider)
    }
}
