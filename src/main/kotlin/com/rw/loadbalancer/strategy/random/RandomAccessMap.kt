package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.provider.Identifiable

/**
 * A whole new data structure just to select a random element?!
 *
 * Well, yes, maybe it's worth it? Should benchmark. \
 * Look-up must be extremely fast; hence, the Map of Providers by ID.
 * But, Map doesn't support random access; hence, this data structure
 *
 * I could have easily written it like:
 * ```
 * val providers: Array<Provider> = providersById.values.toTypedArray() // copy
 * val randomIndex: Int = (Math.random() * providersById.size).toInt()
 * val randomProvider: Provider = providers[randomIndex]
 * ```
 */
class RandomAccessMap<T : Identifiable> {
    private val list: ArrayList<T> = ArrayList()
    private val idToListIndex: HashMap<String, Int?> = HashMap()

    val random: T?
        get() {
            val index = (Math.random() * list.size).toInt()
            return list.getOrNull(index)
        }

    fun insert(value: T): Boolean {
        val id = value.getId()
        return if (idToListIndex.containsKey(id)) {
            false
        } else {
            idToListIndex[id] = list.size
            list.add(value)
            true
        }
    }

    fun remove(value: T): Boolean {
        val id = value.getId()
        val index = idToListIndex[id]
        return if (index != null) {
            // fast delete in ArrayList: swap with last element then remove last element
            val lastValue = list[list.size - 1]
            list[index] = lastValue
            idToListIndex[lastValue.getId()] = index
            list.removeAt(list.size - 1)

            idToListIndex.remove(id)
            true
        } else false
    }
}
