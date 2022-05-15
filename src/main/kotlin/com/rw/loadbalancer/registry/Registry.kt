package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.provider.ProviderDelegate
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.heartbeat.HeartBeatChecker
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

const val MAX_ALLOWED_PROVIDERS: Int = 10

/**
 * This is primarily a Map of the Providers by their ID.
 *
 * The Registry notifies the Subscriber about currently active Providers.
 */
class Registry(
    private val heartBeatChecker: HeartBeatChecker,
    private val registrationUpdatesSubscriber: RegistrationUpdatesSubscriber
) {
    private val providersDelegatesById: ConcurrentHashMap<String, ProviderDelegate<*>> = ConcurrentHashMap()
    private val activeProvidersCounter: AtomicInteger = AtomicInteger(0)

    init {
        heartBeatChecker.start(this)
    }

    fun registerProvider(provider: Provider<*>): String {
        val id = provider.getId()
        if (providersDelegatesById.size < MAX_ALLOWED_PROVIDERS) {
            val providerDelegate = ProviderDelegate(provider)
            providersDelegatesById[id] = providerDelegate
            registrationUpdatesSubscriber.added(providerDelegate)
            if (providerDelegate.isActive()) {
                activeProvidersCounter.incrementAndGet()
            }
            return id
        } else {
            throw RegistrationException(
                "Failed to register provider $id. Already at full capacity: $MAX_ALLOWED_PROVIDERS"
            )
        }
    }

    fun reactivateProvider(id: String) {
        val providerDelegate: ProviderDelegate<*>? = providersDelegatesById[id]
        if (providerDelegate?.activate() == true) {
            registrationUpdatesSubscriber.added(providerDelegate)
            activeProvidersCounter.incrementAndGet()
        }
    }

    fun deactivateProvider(id: String) {
        val providerDelegate: ProviderDelegate<*>? = providersDelegatesById[id]
        if (providerDelegate?.deactivate() == true) {
            registrationUpdatesSubscriber.removed(providerDelegate)
            activeProvidersCounter.decrementAndGet()
        }
    }

    fun listProviders(): List<ProviderInfo> {
        return heartBeatChecker.listProviders()
    }

    val providersDelegatesCopy: Array<ProviderDelegate<*>>
        get() {
            // copy to avoid concurrent iteration/modification
            return providersDelegatesById.values.toTypedArray()
        }

    val activeProvidersCount: Int
        get() {
            return activeProvidersCounter.get()
        }

    fun shutdown() {
        heartBeatChecker.stop()
    }
}
