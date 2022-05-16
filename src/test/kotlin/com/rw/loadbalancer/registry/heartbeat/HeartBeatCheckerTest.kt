package com.rw.loadbalancer.registry.heartbeat

import com.rw.loadbalancer.defaultAwait
import com.rw.loadbalancer.internal.TestProvider
import com.rw.loadbalancer.provider.ProviderDelegate
import com.rw.loadbalancer.registry.RegistrationUpdatesSubscriber
import com.rw.loadbalancer.registry.Registry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class HeartBeatCheckerTest {
    @Test
    fun `checker should deactivate provider if one heart beat is missed`() {
        // given
        val heartBeatChecker = HeartBeatChecker(500)

        val testProviderId = "test-provider"
        val testProvider = TestProvider(testProviderId)

        val addedCalled = AtomicBoolean(false)
        val removedCalled = AtomicBoolean(false)
        val testSubscriber = object : RegistrationUpdatesSubscriber {
            override fun added(providerDelegate: ProviderDelegate) {
                assertThat(providerDelegate.getId()).isEqualTo(testProviderId)
                addedCalled.set(true)
            }

            override fun removed(providerDelegate: ProviderDelegate) {
                assertThat(providerDelegate.getId()).isEqualTo(testProviderId)
                assertThat(providerDelegate.isActive()).isFalse
                removedCalled.set(true)
            }
        }

        val registry = Registry(heartBeatChecker, testSubscriber)
        registry.registerProvider(testProvider)
        assertThat(addedCalled).isTrue

        try {
            heartBeatChecker.start(registry)

            // when
            testProvider.overrideHealth(false)

            defaultAwait(pollInterval = Duration.ofMillis(250)) {
                // then
                assertThat(removedCalled).isTrue
            }
        } finally {
            heartBeatChecker.stop()
        }
    }
}
