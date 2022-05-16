package com.rw.loadbalancer.strategy.roundrobin

import com.rw.loadbalancer.RegistryAwareSelectionStrategy
import com.rw.loadbalancer.provider.ProviderDelegate
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class RoundRobinStrategy : RegistryAwareSelectionStrategy {
    private val readWriteLock = ReentrantReadWriteLock()
    private val list: ArrayList<ProviderDelegate> = ArrayList()
    private val index: AtomicInteger = AtomicInteger(0)

    override fun next(): ProviderDelegate? {
        readWriteLock.read {
            index.updateAndGet { v -> if (v >= list.size) 0 else v }
            return list.getOrNull(index.getAndIncrement())
        }
    }

    override fun added(providerDelegate: ProviderDelegate) {
        readWriteLock.write {
            list.add(providerDelegate)
        }
    }

    override fun removed(providerDelegate: ProviderDelegate) {
        readWriteLock.write {
            list.remove(providerDelegate)
        }
    }
}
