package com.rw.loadbalancer.registry.heartbeat

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.provider.ProviderDelegate
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.Registry
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class HeartBeatChecker(private val checkPeriodInMilliseconds: Long) {
    private lateinit var registry: Registry
    private val providersHealthById = ConcurrentHashMap<String, HealthState>()

    private val executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun start(registry: Registry) {
        this.registry = registry

        val providersCopy: Array<Provider> = registry.providersDelegates.toTypedArray()
        providersCopy.forEach { provider ->
            providersHealthById[provider.getId()] = initialHealthState(provider)
        }
        executorService.scheduleAtFixedRate(
            ::periodicHeartBeatCheck, 0, checkPeriodInMilliseconds, TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        executorService.shutdown()
    }

    fun listProviders(): List<ProviderInfo> {
        val providersCopy: Array<ProviderDelegate> = registry.providersDelegates.toTypedArray()
        return providersCopy.map { provider ->
            val id = provider.getId()
            ProviderInfo(id, provider.isActive(), providersHealthById.getOrDefault(id, initialHealthState(provider)))
        }
    }

    private fun initialHealthState(provider: Provider): HealthState {
        return if (checkHeartBeat(provider)) {
            HealthState.ALIVE
        } else {
            HealthState.DEAD
        }
    }

    private fun periodicHeartBeatCheck() {
        // copy to avoid concurrent iteration/modification
        val providersCopy: Array<Provider> = registry.providersDelegates.toTypedArray()

        providersCopy.forEach { provider ->
            val id = provider.getId()
            if (!checkHeartBeat(provider)) {
                declareDead(provider)
            } else {
                val lastRecordedHealthState = providersHealthById.getOrDefault(id, HealthState.ALIVE)
                when (lastRecordedHealthState) {
                    HealthState.DEAD -> {
                        providersHealthById[id] = HealthState.PENDING_RESURRECTION
                    }
                    HealthState.PENDING_RESURRECTION -> {
                        resurrect(provider)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    private fun declareDead(provider: Provider) {
        registry.deactivateProvider(provider.getId())
        providersHealthById[provider.getId()] = HealthState.DEAD
    }

    private fun resurrect(provider: Provider) {
        registry.reactivateProvider(provider.getId())
        providersHealthById[provider.getId()] = HealthState.ALIVE
    }

    private fun checkHeartBeat(provider: Provider): Boolean {
        return try {
            provider.check()
        } catch (ignore: Exception) { // supposedly could fail
            false
        }
    }
}
