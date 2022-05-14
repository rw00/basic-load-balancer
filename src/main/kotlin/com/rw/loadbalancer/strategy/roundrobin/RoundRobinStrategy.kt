package com.rw.loadbalancer.strategy.roundrobin

import com.rw.loadbalancer.RegistryAwareStrategy
import com.rw.loadbalancer.provider.Provider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class RoundRobinStrategy : RegistryAwareStrategy {
    private val list: MutableList<Provider> = CopyOnWriteArrayList()
    private val index: AtomicInteger = AtomicInteger(-1)

    override fun registered(provider: Provider) {
        list.add(provider)
    }

    override fun next(): Provider {
        index.compareAndSet(list.size - 1, -1)
        return list[index.incrementAndGet()]
    }

    override fun unregistered(provider: Provider) {
        list.remove(provider)
        index.set(-1)
    }
}
