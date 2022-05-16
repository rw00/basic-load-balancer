package com.rw.loadbalancer.strategy.roundrobin

import com.rw.loadbalancer.internal.TestProvider
import com.rw.loadbalancer.provider.ProviderDelegate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoundRobinStrategyTest {
    @Test
    fun `roundRobin strategy should loop over the elements in order repeatedly`() {
        // given
        val roundRobinStrategy = RoundRobinStrategy()
        val provider1Id = "1"
        val provider2Id = "2"
        val provider3Id = "3"
        roundRobinStrategy.added(ProviderDelegate(TestProvider(provider1Id)))
        roundRobinStrategy.added(ProviderDelegate(TestProvider(provider2Id)))
        roundRobinStrategy.added(ProviderDelegate(TestProvider(provider3Id)))
        val providersIds = listOf(provider1Id, provider2Id, provider3Id)

        // when
        repeat(3) {
            providersIds.forEach { providerId ->

                // then
                val nextProvider: ProviderDelegate? = roundRobinStrategy.next()
                assertThat(nextProvider).isNotNull
                nextProvider?.let { providerDelegate ->
                    assertThat(providerDelegate).extracting(ProviderDelegate::getId).isEqualTo(providerId)
                }
            }
        }
    }
}
