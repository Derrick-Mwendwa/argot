package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * `flag` and `list` used to answer `false` and `emptyList()` for a name that was never declared,
 * which turned a typo into a silently wrong parse. They now reject it the way `value` always has.
 */
class StrictLookupTest {

    private fun parsed(vararg argv: String): ParsedValues =
        ArgotEngine.parse(
            CommandSpec(
                programName = "demo",
                params = listOf(
                    OptionSpec(names = listOf("--tag"), converter = StringConverter, multiple = true),
                    OptionSpec(names = listOf("--port"), converter = IntConverter, default = 80),
                    FlagSpec(names = listOf("--verbose")),
                ),
            ),
            arrayOf(*argv),
        )

    @Test
    fun `a declared flag still answers its default when absent`() {
        assertEquals(false, parsed().flag("--verbose"))
        assertEquals(true, parsed("--verbose").flag("--verbose"))
    }

    @Test
    fun `a declared multiple option still answers an empty list when absent`() {
        assertEquals(emptyList(), parsed().list<String>("--tag"))
        assertEquals(listOf("a", "b"), parsed("--tag", "a", "--tag", "b").list<String>("--tag"))
    }

    @Test
    fun `an undeclared name is rejected rather than answered`() {
        val values = parsed()
        assertTrue("no parameter named '--nope'" in assertFailsWith<IllegalArgumentException> {
            values.flag("--nope")
        }.message.orEmpty())
        assertTrue("no parameter named '--nope'" in assertFailsWith<IllegalArgumentException> {
            values.list<String>("--nope")
        }.message.orEmpty())
    }

    @Test
    fun `asking for the wrong kind of parameter is rejected`() {
        val values = parsed()
        // --port holds an Int and --verbose a Boolean; neither used to complain, they just answered
        // false and emptyList().
        assertTrue("not a flag" in assertFailsWith<IllegalArgumentException> {
            values.flag("--port")
        }.message.orEmpty())
        assertTrue("not a 'multiple' parameter" in assertFailsWith<IllegalArgumentException> {
            values.list<String>("--verbose")
        }.message.orEmpty())
    }
}

/** A converter's own explanation now reaches the user instead of being discarded. */
class ConverterMessageTest {

    private object Duration : Converter<Long> {
        override val typeName: String = "Duration"
        override fun convert(raw: String): Long =
            Regex("""^(\d+)s$""").matchEntire(raw)?.groupValues?.get(1)?.toLong()
                ?: throw ArgotConversionException("'$raw' is not a duration (expected 30s)")
    }

    private object Exploding : Converter<String> {
        override val typeName: String = "Exploding"
        override fun convert(raw: String): String = throw IllegalStateException("internal detail")
    }

    private fun parse(converter: Converter<*>, raw: String) =
        ArgotEngine.parse(
            CommandSpec(
                programName = "demo",
                params = listOf(OptionSpec(names = listOf("--at"), converter = converter)),
            ),
            arrayOf("--at", raw),
        )

    @Test
    fun `the converter's message replaces the generic wording`() {
        val e = assertFailsWith<ArgotParseException.InvalidValue> { parse(Duration, "soon") }
        assertEquals("invalid value for --at: 'soon' is not a duration (expected 30s)", e.message)
        assertEquals("'soon' is not a duration (expected 30s)", e.detail)
    }

    @Test
    fun `the original exception is kept as the cause`() {
        val e = assertFailsWith<ArgotParseException.InvalidValue> { parse(Duration, "soon") }
        assertTrue(e.cause is ArgotConversionException, "cause was ${e.cause}")
    }

    @Test
    fun `anything other than a conversion exception keeps the generic wording`() {
        val e = assertFailsWith<ArgotParseException.InvalidValue> { parse(Exploding, "x") }
        assertEquals("invalid value 'x' for --at (expected Exploding)", e.message)
        assertEquals(null, e.detail)
        assertTrue(e.cause is IllegalStateException, "cause was ${e.cause}")
    }
}
