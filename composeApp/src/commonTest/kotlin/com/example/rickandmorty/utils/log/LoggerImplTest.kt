package com.example.rickandmorty.utils.log

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [LoggerImpl].
 */
class LoggerImplTest {

    private data class RecordedCall(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?
    )

    private fun createRecordedCall(
        level: LogLevel = LogLevel.DEBUG,
        tag: String = "MyTag",
        message: String = "hello",
        throwable: Throwable? = null
    ) = RecordedCall(level, tag, message, throwable)

    private val defaultRecordedCall = createRecordedCall()

    private val calls = mutableListOf<RecordedCall>()

    private val logger = LoggerImpl(tag = "MyTag") { level, tag, message, throwable ->
        calls += RecordedCall(level, tag, message, throwable)
    }

    @Test
    fun `debug writes DEBUG level with the tag and no throwable`() {
        // When
        logger.debug("hello")

        // Then
        assertEquals(listOf(defaultRecordedCall), calls)
    }

    @Test
    fun `info writes INFO level with the tag and no throwable`() {
        // When
        logger.info("hello")

        // Then
        assertEquals(listOf(defaultRecordedCall.copy(level = LogLevel.INFO)), calls)
    }

    @Test
    fun `warning writes WARNING level with no throwable by default`() {
        // When
        logger.warning("hello")

        // Then
        assertEquals(listOf(defaultRecordedCall.copy(level = LogLevel.WARNING)), calls)
    }

    @Test
    fun `warning forwards the given throwable`() {
        // Given
        val throwable = IllegalStateException("boom")

        // When
        logger.warning("hello", throwable)

        // Then
        assertEquals(
            listOf(defaultRecordedCall.copy(level = LogLevel.WARNING, throwable = throwable)),
            calls
        )
    }

    @Test
    fun `error writes ERROR level with no throwable by default`() {
        // When
        logger.error("hello")

        // Then
        assertEquals(listOf(defaultRecordedCall.copy(level = LogLevel.ERROR)), calls)
    }

    @Test
    fun `error forwards the given throwable`() {
        // Given
        val throwable = IllegalStateException("boom")

        // When
        logger.error("hello", throwable)

        // Then
        assertEquals(
            listOf(defaultRecordedCall.copy(level = LogLevel.ERROR, throwable = throwable)),
            calls
        )
    }

    @Test
    fun `each call writes exactly once`() {
        // When
        logger.debug("a")
        logger.info("b")

        // Then
        assertEquals(2, calls.size)
    }
}
