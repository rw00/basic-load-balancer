package com.rw.loadbalancer

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.Registry
import com.rw.loadbalancer.registry.heartbeat.HeartBeatChecker
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy

private const val DEFAULT_HEART_BEAT_CHECK_PERIOD_MILLISECONDS: Long = 2 * 100

class LoadBalancer private constructor(
    private val registryAwareStrategy: RegistryAwareStrategy,
    heartBeatChecker: HeartBeatChecker
) {
    private val registry: Registry = Registry(heartBeatChecker, registryAwareStrategy)

    fun registerProvider(provider: Provider): String {
        return registry.registerProvider(provider)
    }

    fun get(): String {
        try {
            val provider: Provider = registryAwareStrategy.next()
            return provider.get()
        } catch (e: IndexOutOfBoundsException) {
            throw NoAvailableProvidersException("There are no available providers")
        }
    }

    fun listRegisteredProviders(): List<ProviderInfo> {
        return registry.listProviders()
    }

    fun reactivateProvider(id: String) {
        registry.reactivateProvider(id)
    }

    fun deactivateProvider(id: String) {
        registry.deactivateProvider(id)
    }

    fun shutdown() {
        registry.shutdown()
    }

    data class Builder(
        var registryAwareStrategy: RegistryAwareStrategy = RoundRobinStrategy(),
        var heartBeatCheckPeriodInMilliseconds: Long = DEFAULT_HEART_BEAT_CHECK_PERIOD_MILLISECONDS
    ) {
        fun registryAwareStrategy(registryAwareStrategy: RegistryAwareStrategy) =
            apply { this.registryAwareStrategy = registryAwareStrategy }

        fun heartBeatCheckPeriodInMilliseconds(heartBeatCheckPeriodInMilliseconds: Long) =
            apply { this.heartBeatCheckPeriodInMilliseconds = heartBeatCheckPeriodInMilliseconds }

        fun build(): LoadBalancer {
            val heartBeatChecker = HeartBeatChecker(heartBeatCheckPeriodInMilliseconds)
            return LoadBalancer(registryAwareStrategy, heartBeatChecker)
        }
    }
}
