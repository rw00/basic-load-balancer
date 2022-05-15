package com.rw.loadbalancer.strategy.roundrobin

import com.rw.loadbalancer.RegistryAwareSelectionStrategy
import com.rw.loadbalancer.provider.ProviderDelegate
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class RoundRobinStrategy : RegistryAwareSelectionStrategy {
    private val readWriteLock = ReentrantReadWriteLock()
    private val list: ArrayList<ProviderDelegate<*>> = ArrayList()
    private var index: Int = 0

    override fun <T> next(): ProviderDelegate<T>? {
        readWriteLock.read {
            val result = list.getOrNull(index % list.size)
            index = (index + 1) % list.size
            return result as ProviderDelegate<T>?
        }
    }

    override fun added(providerDelegate: ProviderDelegate<*>) {
        readWriteLock.write {
            list.add(providerDelegate)
        }
    }

    override fun removed(providerDelegate: ProviderDelegate<*>) {
        readWriteLock.write {
            list.remove(providerDelegate)
        }
    }
}
