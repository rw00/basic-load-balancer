package com.rw.loadbalancer.provider

import com.rw.loadbalancer.registry.heartbeat.HealthState

data class ProviderInfo(
    val id: String,
    val active: Boolean,
    val state: HealthState
)
