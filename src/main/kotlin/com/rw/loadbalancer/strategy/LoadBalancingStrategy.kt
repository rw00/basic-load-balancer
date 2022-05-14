package com.rw.loadbalancer.strategy

import com.rw.loadbalancer.provider.Provider

interface LoadBalancingStrategy {
    fun next(): Provider
}
