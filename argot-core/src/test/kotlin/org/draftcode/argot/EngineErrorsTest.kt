package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EngineErrorsTest {

    private fun spec() = CommandSpec(
        programName = "demo",
        params = listOf(
            OptionSpec(names = listOf("--port", "-p"), converter = IntConverter, required = true),
            OptionSpec(names = listOf("--tag"), converter = StringConverter, multiple = true),
            ArgumentSpec(name = "src", converter = StringConverter),
        ),
    )

    private fun parse(vararg argv: String) = ArgotEngine.parse(spec(), arrayOf(*argv))

    @Test
    fun missingRequiredOption() {
        val e = assertFailsWith<ArgotParseException.MissingRequiredOption> { parse("src.kt") }
        assertEquals("--port", e.name)
    }

    @Test
    fun missingRequiredArgument() {
        val e = assertFailsWith<ArgotParseException.MissingRequiredArgument> { parse("--port", "8080") }
        assertEquals("src", e.name)
    }

    @Test
    fun unknownOption() {
        val e = assertFailsWith<ArgotParseException.UnknownOption> { parse("--port", "8080", "--nope", "src.kt") }
        assertEquals("--nope", e.name)
    }

    @Test
    fun unknownShortOption() {
        val e = assertFailsWith<ArgotParseException.UnknownOption> { parse("--port", "8080", "-z", "src.kt") }
        assertEquals("-z", e.name)
    }

    @Test
    fun invalidIntValue() {
        val e = assertFailsWith<ArgotParseException.InvalidValue> { parse("--port", "abc", "src.kt") }
        assertEquals("--port", e.name)
        assertEquals("abc", e.raw)
        assertEquals("Int", e.expectedType)
    }

    @Test
    fun missingValueAtEndOfInput() {
        val e = assertFailsWith<ArgotParseException.MissingValue> { parse("--port") }
        assertEquals("--port", e.name)
    }

    @Test
    fun duplicateSingleValuedOption() {
        assertFailsWith<ArgotParseException.DuplicateValue> { parse("--port", "8080", "--port", "9090", "src.kt") }
    }

    @Test
    fun multipleOptionDoesNotErrorOnRepeat() {
        val v = parse("--port", "8080", "--tag", "a", "--tag", "b", "src.kt")
        assertEquals(listOf("a", "b"), v.list<String>("--tag"))
    }

    @Test
    fun tooManyArguments() {
        // `src` is the only positional; a second positional is unexpected.
        val e = assertFailsWith<ArgotParseException.TooManyArguments> { parse("--port", "8080", "a", "b") }
        assertEquals(listOf("b"), e.extra)
    }

    @Test
    fun usageIsAttachedToErrors() {
        val e = assertFailsWith<ArgotParseException.MissingRequiredOption> { parse("src.kt") }
        assertNotNull(e.usage)
        assertTrue("Usage: demo" in e.usage!!)
    }
}
