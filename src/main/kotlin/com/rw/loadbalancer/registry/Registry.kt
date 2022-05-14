package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider
import com.rw.loadbalancer.provider.ProviderDelegate
import com.rw.loadbalancer.provider.ProviderInfo
import com.rw.loadbalancer.registry.heartbeat.HeartBeatChecker
import java.util.concurrent.ConcurrentHashMap

const val MAX_ALLOWED_PROVIDERS: Int = 10

class Registry(
    private val heartBeatChecker: HeartBeatChecker,
    private val providerRegistrationSubscriber: ProviderRegistrationSubscriber
) {
    private val providersMap: ConcurrentHashMap<String, ProviderDelegate> = ConcurrentHashMap()

    val providersDelegates: Collection<ProviderDelegate>
        get() {
            return providersMap.values
        }

    init {
        heartBeatChecker.start(this)
    }

    fun registerProvider(provider: Provider): String {
        val id = provider.getId()
        if (providersMap.size < MAX_ALLOWED_PROVIDERS) {
            val providerDelegate = ProviderDelegate(provider)
            providersMap[id] = providerDelegate
            providerRegistrationSubscriber.added(providerDelegate)
            return id
        } else {
            throw RegistrationException(
                "Failed to register provider $id. Already at full capacity: $MAX_ALLOWED_PROVIDERS"
            )
        }
    }

    fun reactivateProvider(id: String) {
        val providerDelegate: ProviderDelegate? = providersMap[id]
        if (providerDelegate?.activate() == true) {
            providerRegistrationSubscriber.added(providerDelegate)
        }
    }

    fun deactivateProvider(id: String) {
        val providerDelegate: ProviderDelegate? = providersMap[id]
        if (providerDelegate?.deactivate() == true) {
            providerRegistrationSubscriber.removed(providerDelegate)
        }
    }

    fun listProviders(): List<ProviderInfo> {
        return heartBeatChecker.listProviders()
    }

    fun shutdown() {
        heartBeatChecker.stop()
    }
}
