package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider

const val MAX_ALLOWED_PROVIDERS: Int = 10

class Registry(private val registrationCallback: RegistrationCallback) {
    private val providersMap: MutableMap<String, Provider> = LinkedHashMap()

    fun registerProvider(provider: Provider) { // TODO fix thread-safety
        if (providersMap.size < MAX_ALLOWED_PROVIDERS) {
            providersMap[provider.getId()] = provider
            registrationCallback.call(provider)
        } else {
            throw RegistrationException(
                "Failed to register provider ${provider.getId()}. Already at full capacity: $MAX_ALLOWED_PROVIDERS"
            )
        }
    }
}
