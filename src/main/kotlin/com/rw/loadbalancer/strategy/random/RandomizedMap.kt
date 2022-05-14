package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.provider.Provider

class RandomizedMap<T : Provider> {
    private val list: MutableList<T> = ArrayList()
    private val idToIndex: MutableMap<String, Int?> = HashMap()

    fun insert(value: T): Boolean {
        val id = value.getId()
        return if (idToIndex.containsKey(id)) {
            false
        } else {
            idToIndex[id] = list.size
            list.add(value)
            true
        }
    }

    fun remove(value: T): Boolean {
        val id = value.getId()
        val index = idToIndex[id]
        return if (index != null) {
            // fast delete in ArrayList: swap with last element then remove last element
            val lastValue = list[list.size - 1]
            list[index] = lastValue
            idToIndex[lastValue.getId()] = index
            list.removeAt(list.size - 1)

            idToIndex.remove(id)
            true
        } else false
    }

    val random: T
        get() {
            val index = (Math.random() * list.size).toInt()
            return list[index]
        }
}
