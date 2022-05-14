package com.rw.loadbalancer.provider

import java.util.UUID

open class UuidProvider : Provider {
    private val uuid: String = UUID.randomUUID().toString()

    override fun getId(): String = uuid

    override fun get(): String {
        return uuid
    }

    override fun check(): Boolean { // this is supposedly a remote call; it just needs to complete
        return true
    }
}
