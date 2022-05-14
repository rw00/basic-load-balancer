package com.rw.loadbalancer

import com.rw.loadbalancer.provider.UuidProvider
import com.rw.loadbalancer.registry.MAX_ALLOWED_PROVIDERS
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoundRobinLoadBalancerTest {

    @Test
    fun `roundRobin loadBalancer iterates inorder over providers`() {
        val loadBalancer = createRoundRobinLoadBalancer()

        val expectedProvidersIds: MutableList<String> = mutableListOf()
        repeat(MAX_ALLOWED_PROVIDERS) {
            val provider = UuidProvider()
            expectedProvidersIds.add(provider.getId())
            loadBalancer.registerProvider(provider)
        }

        expectedProvidersIds.forEach { providerId ->
            assertThat(loadBalancer.get()).isEqualTo(providerId)
        }
        // cycle
        expectedProvidersIds.forEach { providerId ->
            assertThat(loadBalancer.get()).isEqualTo(providerId)
        }
    }

    private fun createRoundRobinLoadBalancer(): LoadBalancer {
        return LoadBalancerBuilder().registryAwareStrategy(RoundRobinStrategy()).build()
    }
}
