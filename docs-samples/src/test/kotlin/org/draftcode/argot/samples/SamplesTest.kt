package org.draftcode.argot.samples

import kotlin.test.Test
import kotlin.test.assertEquals
import org.draftcode.argot.HelpRequested
import org.draftcode.argot.parse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Asserts the output the docs site claims each sample produces. A sample that compiles but prints
 * something other than what the prose says is still a broken doc, so the claims live here.
 */
class SamplesTest {

    @Test
    fun `greet defaults to a single quiet hello`() {
        val args = GreetArgs().parse(emptyArray())
        assertEquals(listOf("Hello, world!"), greetings(args))
    }

    @Test
    fun `greet honours name, count and loud`() {
        val args = GreetArgs().parse(arrayOf("--name", "Ada", "--count", "2", "--loud"))
        assertEquals(listOf("HELLO, ADA!", "HELLO, ADA!"), greetings(args))
    }

    @Test
    fun `greet accepts short names`() {
        val args = GreetArgs().parse(arrayOf("-n", "Ada", "-c", "3"))
        assertEquals(3, greetings(args).size)
    }

    @Test
    fun `greet renders the help text the docs show`() {
        val help = assertFailsWith<HelpRequested> { GreetArgs().parse(arrayOf("--help")) }.rendered
        assertTrue("Usage: greet [options]" in help, help)
        assertTrue("--name, -n <String>" in help, help)
        assertTrue("(default: world)" in help, help)
    }

    @Test
    fun `serve parses through the generated parser`() {
        val args = parseServeArgs(arrayOf("--port", "8080", "a.txt", "b.txt"))
        assertEquals("serving 2 file(s) on 0.0.0.0:8080", describe(args))
    }

    @Test
    fun `serve requires a port`() {
        assertFailsWith<org.draftcode.argot.ArgotParseException.MissingRequiredOption> {
            parseServeArgs(arrayOf("a.txt"))
        }
    }
}
