package com.rw.loadbalancer.strategy.random

import com.rw.loadbalancer.provider.Provider

class RandomizedMap<T : Provider> {
    private val list: ArrayList<T> = ArrayList()
    private val idToListIndex: HashMap<String, Int?> = HashMap()

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

    val random: T
        get() {
            val index = (Math.random() * list.size).toInt()
            return list[index]
        }
}
