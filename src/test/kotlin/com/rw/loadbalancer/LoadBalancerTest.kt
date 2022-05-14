package com.rw.loadbalancer

import com.rw.loadbalancer.provider.UuidProvider
import com.rw.loadbalancer.registry.MAX_ALLOWED_PROVIDERS
import com.rw.loadbalancer.registry.RegistrationException
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class LoadBalancerTest {
    @Test
    fun `loadBalancer can register a max of 10 providers`() {
        val loadBalancer = LoadBalancer()
        repeat(MAX_ALLOWED_PROVIDERS) {
            loadBalancer.registerProvider(UuidProvider())
        }
        assertThrows<RegistrationException> { loadBalancer.registerProvider(UuidProvider()) }
    }

    @Test
    fun `loadBalancer randomly selects a provider and returns result`() {
        val loadBalancer = LoadBalancer()

        val provider1 = UuidProvider()
        val provider2 = UuidProvider()
        loadBalancer.registerProvider(provider1)
        loadBalancer.registerProvider(provider2)

        val expectedProvidersIds = setOf(provider1.getId(), provider2.getId())
        val providersIds: MutableSet<String> = HashSet()
        await().atMost(expectedProvidersIds.size.toLong(), SECONDS).pollInterval(10, MILLISECONDS).until {
            providersIds.add(loadBalancer.get())

            expectedProvidersIds == providersIds
        }
    }
}
