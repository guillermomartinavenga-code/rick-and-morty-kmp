package com.example.rickandmorty.utils.log

import org.slf4j.LoggerFactory

actual fun platformLog(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
) {
    val log = LoggerFactory.getLogger(tag)
    when (level) {
        LogLevel.DEBUG -> log.debug(message)
        LogLevel.INFO -> log.info(message)
        LogLevel.WARNING -> log.warn(message, throwable)
        LogLevel.ERROR -> log.error(message, throwable)
    }
}
