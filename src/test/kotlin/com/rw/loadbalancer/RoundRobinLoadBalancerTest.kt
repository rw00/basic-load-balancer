package com.rw.loadbalancer

import com.rw.loadbalancer.internal.TestProvider
import com.rw.loadbalancer.provider.UuidProvider
import com.rw.loadbalancer.registry.MAX_ALLOWED_PROVIDERS
import com.rw.loadbalancer.strategy.roundrobin.RoundRobinStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

class RoundRobinLoadBalancerTest {

    @Test
    fun `roundRobin loadBalancer should iterate inorder over providers repeatedly`() {
        // given
        val loadBalancer = createRoundRobinLoadBalancer()
        val expectedProvidersIds: MutableList<String> = mutableListOf()
        repeat(MAX_ALLOWED_PROVIDERS) {
            val provider = UuidProvider()
            expectedProvidersIds.add(provider.getId())
            loadBalancer.registerProvider(provider)
        }

        // when
        checkRoundRobinCycle(expectedProvidersIds, loadBalancer)
        checkRoundRobinCycle(expectedProvidersIds, loadBalancer)
    }

    @Test
    fun `loadBalancer should check for cluster capacity limit`() {
        // given
        val loadBalancer = createRoundRobinLoadBalancerBuilder().maxProviderConcurrency(1).build()
        val providerCustomId = "test-provider"
        val provider = object : TestProvider(providerCustomId) {
            override fun get(): String {
                Thread.sleep(500)
                return providerCustomId
            }
        }
        loadBalancer.registerProvider(provider)

        val oneCallAccepted = AtomicBoolean(false)
        val oneCallRejected = AtomicBoolean(false)

        // when
        val concurrency = 2
        repeat(concurrency) {
            val completableFuture = loadBalancer.get()
            completableFuture.whenComplete { result, throwable ->
                if (throwable == null) {
                    assertThat(result).isEqualTo(providerCustomId)
                    oneCallAccepted.set(true)
                } else {
                    oneCallRejected.set(true)
                }
            }
        }

        // then
        defaultAwait {
            assertThat(oneCallAccepted).isTrue
            assertThat(oneCallRejected).isTrue
        }
    }

    private fun checkRoundRobinCycle(expectedProvidersIds: MutableList<String>, loadBalancer: LoadBalancer<String>) {
        // then
        expectedProvidersIds.forEach { providerId ->
            val completableFuture = loadBalancer.get()
            completableFuture.whenComplete { result, _ ->
                assertThat(result).isEqualTo(providerId)
            }
        }
    }

    private fun createRoundRobinLoadBalancer(): LoadBalancer<String> {
        return createRoundRobinLoadBalancerBuilder().build()
    }

    private fun createRoundRobinLoadBalancerBuilder(): LoadBalancer.Builder<String> {
        return LoadBalancer.Builder<String>().registryAwareSelectionStrategy(RoundRobinStrategy())
    }
}
