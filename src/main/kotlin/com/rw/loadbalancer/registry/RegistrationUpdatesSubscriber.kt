package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.ProviderDelegate

interface RegistrationUpdatesSubscriber {
    fun added(providerDelegate: ProviderDelegate)
    fun removed(providerDelegate: ProviderDelegate)
}
