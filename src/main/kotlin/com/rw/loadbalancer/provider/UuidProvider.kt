package com.rw.loadbalancer.provider

import java.util.UUID

class UuidProvider : Provider {
    private val uuid: String = UUID.randomUUID().toString()

    override fun get(): String = uuid
}
