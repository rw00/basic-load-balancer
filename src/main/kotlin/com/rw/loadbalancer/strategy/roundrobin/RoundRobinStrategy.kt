package com.rw.loadbalancer.strategy.roundrobin

import com.rw.loadbalancer.RegistryAwareStrategy
import com.rw.loadbalancer.provider.ProviderDelegate
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class RoundRobinStrategy : RegistryAwareStrategy {
    private val list: MutableList<ProviderDelegate> = CopyOnWriteArrayList()
    private val index: AtomicInteger = AtomicInteger(-1)

    override fun next(): ProviderDelegate {
        index.compareAndSet(list.size - 1, -1)
        return list[index.incrementAndGet()]
    }

    override fun added(provider: ProviderDelegate) {
        list.add(provider)
    }

    override fun removed(provider: ProviderDelegate) {
        list.remove(provider)
        index.set(-1)
    }
}
