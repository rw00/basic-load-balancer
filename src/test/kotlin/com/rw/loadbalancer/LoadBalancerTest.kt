package com.rw.loadbalancer

import com.rw.loadbalancer.provider.UuidProvider
import com.rw.loadbalancer.registry.MAX_ALLOWED_PROVIDERS
import com.rw.loadbalancer.registry.RegistrationException
import com.rw.loadbalancer.strategy.random.RandomizedStrategy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration

class LoadBalancerTest {
    @Test
    fun `loadBalancer can register a max of 10 providers`() {
        val loadBalancer = createRandomizedLoadBalancer()
        repeat(MAX_ALLOWED_PROVIDERS) {
            loadBalancer.registerProvider(UuidProvider())
        }
        assertThatThrownBy { loadBalancer.registerProvider(UuidProvider()) }
            .isInstanceOf(RegistrationException::class.java)
    }

    @Test
    fun `loadBalancer randomly selects a provider and returns result`() {
        val loadBalancer = createRandomizedLoadBalancer()

        val provider1 = UuidProvider()
        val provider2 = UuidProvider()
        loadBalancer.registerProvider(provider1)
        loadBalancer.registerProvider(provider2)

        val expectedProvidersIds = setOf(provider1.getId(), provider2.getId())
        val providersIds: MutableSet<String> = HashSet()

        defaultAwait(atMost = Duration.ofSeconds(expectedProvidersIds.size.toLong())) {
            providersIds.add(loadBalancer.get())

            assertThat(providersIds).isEqualTo(expectedProvidersIds)
        }
    }

    @Test
    fun `loadBalancer properly handles when there are no registered providers`() {
        val loadBalancer = createRandomizedLoadBalancer()

        assertThatThrownBy { loadBalancer.get() }.isInstanceOf(NoAvailableProvidersException::class.java)
    }

    private fun createRandomizedLoadBalancer(): LoadBalancer {
        return LoadBalancer.Builder().registryAwareStrategy(RandomizedStrategy()).build()
    }
}
