package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.ProviderDelegate

interface ProviderRegistrationSubscriber {
    fun added(provider: ProviderDelegate)
    fun removed(provider: ProviderDelegate)
}
