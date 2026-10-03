package com.pictureorganizer.util.log

import android.util.Log
import com.pictureorganizer.BuildConfig

object AppLog {
    private const val PREFIX = "PO"

    fun d(
        tag: String,
        message: String,
    ) {
        if (!BuildConfig.DEBUG) return
        Log.d(formatTag(tag), message)
    }

    fun i(
        tag: String,
        message: String,
    ) {
        if (!BuildConfig.DEBUG) return
        Log.i(formatTag(tag), message)
    }

    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        if (throwable != null) {
            Log.w(formatTag(tag), message, throwable)
        } else {
            Log.w(formatTag(tag), message)
        }
    }

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        if (throwable != null) {
            Log.e(formatTag(tag), message, throwable)
        } else {
            Log.e(formatTag(tag), message)
        }
    }

    private fun formatTag(tag: String): String = "$PREFIX/$tag"
}
