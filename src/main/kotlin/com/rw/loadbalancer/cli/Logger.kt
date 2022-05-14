package com.rw.loadbalancer.cli

object Logger {
    fun logInfo(msg: String) {
        println(msg)
    }

    fun logInfoInline(msg: String) {
        print(msg)
    }

    fun logError(msg: String) {
        System.err.println(msg)
        System.err.flush()
    }

    fun logError(exception: Exception) {
        System.err.println(exception)
    }
}
