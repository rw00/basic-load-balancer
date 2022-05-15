package com.rw.loadbalancer

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.Registry
import com.rw.loadbalancer.registry.heartbeat.HeartBeatChecker
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

const val DEFAULT_HEART_BEAT_CHECK_PERIOD_MILLISEC: Long = 2 * 1000
const val DEFAULT_MAX_PROVIDER_CONCURRENCY: Int = 5

class LoadBalancer<T> private constructor(
    private val registryAwareSelectionStrategy: RegistryAwareSelectionStrategy,
    private val maxProviderConcurrency: Int,
    heartBeatChecker: HeartBeatChecker
) {
    private val registry: Registry = Registry(heartBeatChecker, registryAwareSelectionStrategy)
    private val currentRequestsCounter: AtomicInteger = AtomicInteger(0)
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    fun get(): CompletableFuture<T> {
        return try {
            val activeProvidersCount: Int = registry.activeProvidersCount
            if (maxProviderConcurrency * activeProvidersCount < currentRequestsCounter.incrementAndGet()) {
                return doCompleteExceptionallyNoProviders()
            }
            doGet()
        } catch (e: Exception) {
            // log
            CompletableFuture.failedFuture(IllegalStateException("Unhandled exception!", e))
        }
    }

    fun registerProvider(provider: Provider<T>): String {
        return registry.registerProvider(provider)
    }

    fun reactivateProvider(id: String) {
        registry.reactivateProvider(id)
    }

    fun deactivateProvider(id: String) {
        registry.deactivateProvider(id)
    }

    val registeredProviders: List<ProviderInfo>
        get() {
            return registry.listProviders()
        }

    fun shutdown() {
        executorService.shutdown()
        registry.shutdown()
    }

    private fun doGet(): CompletableFuture<T> {
        val provider: Provider<T>? = registryAwareSelectionStrategy.next()
        return if (provider == null) {
            doCompleteExceptionallyNoProviders()
        } else {
            CompletableFuture.supplyAsync({ doCallProvider(provider) }, executorService)
        }
    }

    private fun doCallProvider(provider: Provider<T>): T {
        return try {
            provider.get()
        } finally {
            decrementCurrentRequestsCount()
        }
    }

    private fun doCompleteExceptionallyNoProviders(): CompletableFuture<T> {
        decrementCurrentRequestsCount()
        return CompletableFuture.failedFuture(NoAvailableProvidersException("There are no available providers"))
    }

    private fun decrementCurrentRequestsCount() {
        currentRequestsCounter.updateAndGet { v -> if (v > 0) v - 1 else v }
    }

    data class Builder<T>(
        var registryAwareSelectionStrategy: RegistryAwareSelectionStrategy = RoundRobinStrategy(),
        var heartBeatCheckPeriodInMilliSec: Long = DEFAULT_HEART_BEAT_CHECK_PERIOD_MILLISEC,
        var maxProviderConcurrency: Int = DEFAULT_MAX_PROVIDER_CONCURRENCY
    ) {
        fun registryAwareSelectionStrategy(registryAwareSelectionStrategy: RegistryAwareSelectionStrategy) =
            apply { this.registryAwareSelectionStrategy = registryAwareSelectionStrategy }

        fun heartBeatCheckPeriodInMilliSec(heartBeatCheckPeriodInMilliSec: Long) =
            apply { this.heartBeatCheckPeriodInMilliSec = heartBeatCheckPeriodInMilliSec }

        fun maxProviderConcurrency(maxProviderConcurrency: Int) =
            apply { this.maxProviderConcurrency = maxProviderConcurrency }

        fun reset() =
            apply {
                this.registryAwareSelectionStrategy = RoundRobinStrategy()
                this.heartBeatCheckPeriodInMilliSec = DEFAULT_HEART_BEAT_CHECK_PERIOD_MILLISEC
                this.maxProviderConcurrency = DEFAULT_MAX_PROVIDER_CONCURRENCY
            }

        fun build(): LoadBalancer<T> {
            val heartBeatChecker = HeartBeatChecker(heartBeatCheckPeriodInMilliSec)
            return LoadBalancer(registryAwareSelectionStrategy, maxProviderConcurrency, heartBeatChecker)
        }
    }
}
