package com.example.rickandmorty.utils.log

fun createLogger(tag: String): Logger = LoggerImpl(tag)

internal class LoggerImpl(
    private val tag: String,
    private val write: (LogLevel, String, String, Throwable?) -> Unit = ::platformLog
) : Logger {
    override fun debug(message: String) = write(LogLevel.DEBUG, tag, message, null)

    override fun info(message: String) = write(LogLevel.INFO, tag, message, null)

    override fun warning(message: String, throwable: Throwable?) =
        write(LogLevel.WARNING, tag, message, throwable)

    override fun error(message: String, throwable: Throwable?) =
        write(LogLevel.ERROR, tag, message, throwable)
}

expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)
