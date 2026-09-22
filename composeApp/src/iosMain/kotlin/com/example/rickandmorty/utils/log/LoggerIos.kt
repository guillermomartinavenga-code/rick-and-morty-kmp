package com.example.rickandmorty.utils.log

import platform.Foundation.NSLog

actual fun platformLog(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
) {
    val prefix = "[${level.name}] $tag:"
    NSLog("$prefix $message")
    throwable?.let { NSLog("$prefix ${it.stackTraceToString()}") }
}
