package com.rw.loadbalancer.registry.heartbeat

import com.rw.loadbalancer.defaultAwait
import com.rw.loadbalancer.provider.ProviderDelegate
import com.rw.loadbalancer.provider.TestUuidProvider
import com.rw.loadbalancer.registry.ProviderRegistrationSubscriber
import com.rw.loadbalancer.registry.Registry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class HeartBeatCheckerTest {
    @Test
    fun `checker should deactivate provider if one heart beat is missed`() {
        val heartBeatChecker = HeartBeatChecker(500)

        val defaultProvider = TestUuidProvider()

        val addedCalled = AtomicBoolean(false)
        val removedCalled = AtomicBoolean(false)
        val subscriber = object : ProviderRegistrationSubscriber {
            override fun added(provider: ProviderDelegate) {
                assertThat(provider.getId()).isEqualTo(defaultProvider.getId())
                addedCalled.set(true)
            }

            override fun removed(provider: ProviderDelegate) {
                assertThat(provider.getId()).isEqualTo(defaultProvider.getId())
                removedCalled.set(true)
            }
        }

        val registry = Registry(heartBeatChecker, subscriber)
        registry.registerProvider(defaultProvider)
        assertThat(addedCalled).isTrue
        try {
            heartBeatChecker.start(registry)
            defaultProvider.overrideHealth(false)

            defaultAwait(pollInterval = Duration.ofMillis(250)) {
                assertThat(removedCalled).isTrue
            }
        } finally {
            heartBeatChecker.stop()
        }
    }
}
