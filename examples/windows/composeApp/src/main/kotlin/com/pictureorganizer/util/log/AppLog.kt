package com.pictureorganizer.util.log

object AppLog {
    fun d(
        tag: String,
        message: String,
    ) {
        println("D/$tag: $message")
    }

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        System.err.println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}
