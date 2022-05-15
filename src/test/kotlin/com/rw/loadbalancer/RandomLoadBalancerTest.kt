package com.rw.loadbalancer

import com.rw.loadbalancer.provider.UuidProvider
import com.rw.loadbalancer.registry.MAX_ALLOWED_PROVIDERS
import com.rw.loadbalancer.registry.RegistrationException
import com.rw.loadbalancer.strategy.random.RandomizedStrategy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration

class RandomLoadBalancerTest {
    @Test
    fun `loadBalancer should register no more than 10 providers`() {
        // given
        val loadBalancer = createRandomizedLoadBalancer()

        // when
        repeat(MAX_ALLOWED_PROVIDERS) {
            loadBalancer.registerProvider(UuidProvider())
        }
        assertThatThrownBy { loadBalancer.registerProvider(UuidProvider()) }
            // then
            .isInstanceOf(RegistrationException::class.java)
    }

    @Test
    fun `loadBalancer should randomly select provider and return result from it asynchronously`() {
        // given
        val loadBalancer = createRandomizedLoadBalancer()

        val provider1 = UuidProvider()
        val provider2 = UuidProvider()
        loadBalancer.registerProvider(provider1)
        loadBalancer.registerProvider(provider2)

        val expectedProvidersIds = setOf(provider1.getId(), provider2.getId())
        val providersIds: MutableSet<String> = HashSet()

        defaultAwait(atMost = Duration.ofSeconds(expectedProvidersIds.size.toLong())) {
            // when
            val result = loadBalancer.get().get()
            providersIds.add(result)

            // then
            assertThat(providersIds).isEqualTo(expectedProvidersIds)
        }
    }

    @Test
    fun `loadBalancer should handle case when there are no registered providers`() {
        // given
        val loadBalancer = createRandomizedLoadBalancer()

        // when
        val completableFuture = loadBalancer.get()

        // then
        completableFuture.whenComplete { _, throwable ->
            assertThat(throwable).isInstanceOf(NoAvailableProvidersException::class.java)
        }
        assertThat(completableFuture).isCompletedExceptionally
    }

    private fun createRandomizedLoadBalancer(): LoadBalancer<String> {
        return LoadBalancer.Builder<String>().registryAwareSelectionStrategy(RandomizedStrategy()).build()
    }
}
