package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.RegistryAwareSelectionStrategy
import com.rw.loadbalancer.provider.ProviderDelegate
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class RandomizedStrategy : RegistryAwareSelectionStrategy {
    private val randomAccessMap: RandomAccessMap<ProviderDelegate> = RandomAccessMap()
    private val readWriteLock = ReentrantReadWriteLock()

    override fun next(): ProviderDelegate? {
        readWriteLock.read {
            return randomAccessMap.random
        }
    }

    override fun added(providerDelegate: ProviderDelegate) {
        readWriteLock.write {
            randomAccessMap.insert(providerDelegate)
        }
    }

    override fun removed(providerDelegate: ProviderDelegate) {
        readWriteLock.write {
            randomAccessMap.remove(providerDelegate)
        }
    }
}
