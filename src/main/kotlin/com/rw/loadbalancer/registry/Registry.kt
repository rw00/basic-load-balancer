package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider
import java.util.concurrent.ConcurrentHashMap

const val MAX_ALLOWED_PROVIDERS: Int = 10

class Registry(
    private val registrationCallback: RegistrationCallback,
    private val unregistrationCallback: UnregistrationCallback
) {
    private val providersMap: ConcurrentHashMap<String, Provider> = ConcurrentHashMap()

    fun registerProvider(provider: Provider) {
        if (providersMap.size < MAX_ALLOWED_PROVIDERS) {
            providersMap[provider.getId()] = provider
            registrationCallback.registered(provider)
        } else {
            throw RegistrationException(
                "Failed to register provider ${provider.getId()}. Already at full capacity: $MAX_ALLOWED_PROVIDERS"
            )
        }
    }

    fun deactivateProvider(provider: Provider) {
        if (provider.deactivate()) {
            unregistrationCallback.unregistered(provider)
        }
    }
}
