package com.rw.loadbalancer.registry.heartbeat

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.provider.ProviderDelegate
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.Registry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * This Checker periodically pings the Providers in the Registry.
 *
 * If a Provider does not respond, the Registry will be requested to remove it from the active pool.
 */
class HeartBeatChecker(private val checkPeriodInMilliseconds: Long) {
    private lateinit var registry: Registry
    private val providersHealthStateById = ConcurrentHashMap<String, HealthState>()

    private val scheduledExecutorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun start(registry: Registry) {
        this.registry = registry
        initialCheck()
        scheduledExecutorService.scheduleAtFixedRate(
            ::periodicHeartBeatCheck, checkPeriodInMilliseconds, checkPeriodInMilliseconds, TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        scheduledExecutorService.shutdown()
    }

    fun listProviders(): List<ProviderInfo> {
        val providersDelegates: Array<ProviderDelegate<*>> = registry.providersDelegatesCopy
        return providersDelegates.map { provider ->
            val id = provider.getId()
            ProviderInfo(
                id,
                provider.isActive(),
                providersHealthStateById.getOrDefault(id, initialHealthState(provider))
            )
        }
    }

    private fun initialCheck() {
        val providers: Array<ProviderDelegate<*>> = registry.providersDelegatesCopy
        providers.forEach { provider ->
            providersHealthStateById[provider.getId()] = initialHealthState(provider)
        }
    }

    private fun initialHealthState(provider: Provider<*>): HealthState {
        return if (heartBeatCheck(provider)) {
            HealthState.ALIVE
        } else {
            HealthState.DEAD
        }
    }

    private fun periodicHeartBeatCheck() {
        val providers: Array<ProviderDelegate<*>> = registry.providersDelegatesCopy

        providers.forEach { provider ->
            val id = provider.getId()
            if (!heartBeatCheck(provider)) {
                declareDead(provider)
            } else {
                val lastRecordedHealthState = providersHealthStateById.getOrDefault(id, HealthState.ALIVE)
                when (lastRecordedHealthState) {
                    HealthState.DEAD -> {
                        providersHealthStateById[id] = HealthState.PENDING_RESURRECTION
                    }
                    HealthState.PENDING_RESURRECTION -> {
                        declareAlive(provider)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    private fun declareDead(provider: Provider<*>) {
        val id = provider.getId()
        registry.deactivateProvider(id)
        providersHealthStateById[id] = HealthState.DEAD
    }

    private fun declareAlive(provider: Provider<*>) {
        val id = provider.getId()
        registry.reactivateProvider(id)
        providersHealthStateById[id] = HealthState.ALIVE
    }

    private fun heartBeatCheck(provider: Provider<*>): Boolean {
        return try {
            // log pinging...
            provider.check()
        } catch (ignore: Exception) { // supposedly could fail
            false
        }
    }
}
