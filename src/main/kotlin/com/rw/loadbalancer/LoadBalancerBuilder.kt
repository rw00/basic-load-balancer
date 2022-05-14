package com.rw.loadbalancer

import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy

data class LoadBalancerBuilder(
    var registryAwareStrategy: RegistryAwareStrategy = RoundRobinStrategy()
) {
    fun registryAwareStrategy(registryAwareStrategy: RegistryAwareStrategy) =
        apply { this.registryAwareStrategy = registryAwareStrategy }

    fun build(): LoadBalancer {
        return LoadBalancer(registryAwareStrategy)
    }
}
