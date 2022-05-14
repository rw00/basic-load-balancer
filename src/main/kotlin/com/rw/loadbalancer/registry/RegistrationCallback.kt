package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider

interface RegistrationCallback {
    fun call(provider: Provider)
}
