package com.rw.loadbalancer.cli

import com.rw.loadbalancer.internal.DebugUtil

object Logger {
    fun logInfo(msg: String) {
        println(msg)
    }

    fun logInfoInline(msg: String) {
        print(msg)
    }

    fun logError(msg: String) {
        System.err.flush()
        System.err.println(msg)
    }

    fun logError(exception: Exception) {
        System.err.println(exception)
        if (DebugUtil.DEBUG_ENABLED) {
            exception.printStackTrace()
        }
    }

    fun logError(throwable: Throwable) { // called from the CompletableFuture
        System.err.println(throwable)
        if (DebugUtil.DEBUG_ENABLED) {
            throwable.printStackTrace()
        }
    }
}
