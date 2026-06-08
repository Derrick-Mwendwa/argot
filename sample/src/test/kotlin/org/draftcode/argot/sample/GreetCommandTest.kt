package org.draftcode.argot.sample

import org.draftcode.argot.ArgotParseException
import org.draftcode.argot.HelpRequested
import org.draftcode.argot.parse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Integration tests for the delegate-style `greet` command entry point (§7). */
class GreetCommandTest {

    @Test
    fun defaultsProduceASingleGreeting() {
        val args = GreetArgs().parse(arrayOf())
        assertEquals(listOf("Hello, world!"), greetingLines(args))
    }

    @Test
    fun nameCountShoutAndExtras() {
        val args = GreetArgs().parse(arrayOf("-n", "Ada", "--count", "2", "--shout", "Bob", "Cara"))
        assertEquals(
            listOf(
                "HELLO, ADA!", "HELLO, BOB!", "HELLO, CARA!",
                "HELLO, ADA!", "HELLO, BOB!", "HELLO, CARA!",
            ),
            greetingLines(args),
        )
    }

    @Test
    fun helpCaseSignalsHelp() {
        assertFailsWith<HelpRequested> { GreetArgs().parse(arrayOf("--help")) }
    }

    @Test
    fun errorCaseRejectsInvalidInt() {
        assertFailsWith<ArgotParseException.InvalidValue> { GreetArgs().parse(arrayOf("--count", "abc")) }
    }
}
