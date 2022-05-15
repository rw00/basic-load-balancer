package com.rw.loadbalancer.provider

import java.util.UUID

class UuidProvider : Provider<String> {
    private val uuid: String = UUID.randomUUID().toString()

    override fun getId(): String = uuid

    override fun get(): String {
        return getId()
    }

    override fun check(): Boolean { // this is supposedly a remote call; it just needs to complete
        return true
    }
}
