package com.example.rickandmorty.utils.log

interface Logger {
    fun debug(message: String)
    fun info(message: String)
    fun warning(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
}
