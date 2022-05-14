package com.rw.loadbalancer

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.registry.Registry
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy

class LoadBalancer private constructor(private val registryAwareStrategy: RegistryAwareStrategy) {
    private val registry: Registry = Registry(registryAwareStrategy, registryAwareStrategy)

    fun registerProvider(provider: Provider) {
        registry.registerProvider(provider)
    }

    fun get(): String {
        try {
            val provider = registryAwareStrategy.next()
            return provider.getId()
        } catch (e: IndexOutOfBoundsException) {
            throw NoAvailableProvidersException("There are no available providers")
        }
    }

    data class Builder(var registryAwareStrategy: RegistryAwareStrategy = RoundRobinStrategy()) {
        fun registryAwareStrategy(registryAwareStrategy: RegistryAwareStrategy) =
            apply { this.registryAwareStrategy = registryAwareStrategy }

        fun build(): LoadBalancer {
            return LoadBalancer(registryAwareStrategy)
        }
    }
}
