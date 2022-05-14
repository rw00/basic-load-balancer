package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider

interface RegistrationCallback {
    fun registered(provider: Provider)
}
