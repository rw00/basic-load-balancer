package com.rw.loadbalancer

import com.rw.loadbalancer.provider.Provider

const val MAX_ALLOWED_PROVIDERS: Int = 10

class LoadBalancer {
    private val providersMap: MutableMap<String, Provider> = LinkedHashMap();

    fun registerProvider(provider: Provider) { // TODO not thread-safe
        if (providersMap.size < MAX_ALLOWED_PROVIDERS) {
            providersMap[provider.get()] = provider
        } else {
            throw ProviderRegistrationException(
                "Failed to register provider ${provider.get()}. Already at full capacity: $MAX_ALLOWED_PROVIDERS"
            )
        }
    }
}
