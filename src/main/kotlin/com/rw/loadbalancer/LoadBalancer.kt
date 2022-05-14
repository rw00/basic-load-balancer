package com.rw.loadbalancer

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.registry.Registry

class LoadBalancer internal constructor(private val registryAwareStrategy: RegistryAwareStrategy) {
    private val registry: Registry = Registry(registryAwareStrategy)

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
}
