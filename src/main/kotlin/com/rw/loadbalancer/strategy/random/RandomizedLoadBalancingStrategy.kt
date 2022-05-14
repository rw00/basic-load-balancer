package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.registry.RegistrationCallback
import com.rw.loadbalancer.strategy.LoadBalancingStrategy

class RandomizedLoadBalancingStrategy : LoadBalancingStrategy, RegistrationCallback {
    private val randomizedMap: RandomizedMap<Provider> = RandomizedMap()

    override fun next(): Provider {
        return randomizedMap.random
    }

    override fun call(provider: Provider) {
        randomizedMap.insert(provider)
    }
}
