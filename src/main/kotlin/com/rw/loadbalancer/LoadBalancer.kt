package com.rw.loadbalancer

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.registry.Registry
import com.rw.loadbalancer.strategy.random.RandomizedLoadBalancingStrategy


class LoadBalancer {
    private val loadBalancingStrategy: RandomizedLoadBalancingStrategy = RandomizedLoadBalancingStrategy()
    private val registry: Registry = Registry(loadBalancingStrategy)

    fun registerProvider(provider: Provider) {
        registry.registerProvider(provider)
    }

    fun get(): String {
        return loadBalancingStrategy.next().getId()
    }
}
