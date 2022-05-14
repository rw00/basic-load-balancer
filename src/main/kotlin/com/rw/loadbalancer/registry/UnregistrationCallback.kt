package com.rw.loadbalancer.registry

import com.rw.loadbalancer.provider.Provider

interface UnregistrationCallback {
    fun unregistered(provider: Provider)
}
