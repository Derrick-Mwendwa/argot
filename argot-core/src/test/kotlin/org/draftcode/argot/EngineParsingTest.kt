package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private enum class Color { RED, GREEN, BLUE }

/**
 * Exercises the core parsing semantics of [ArgotEngine] (§4.5): the various option forms, flags,
 * positionals, defaults, and the full error surface.
 */
class EngineParsingTest {

    /** A representative command: a defaulted option, a required option, a `multiple` option,
     * an enum option, a flag, a required positional, and a trailing `multiple` positional. */
    private fun fullSpec() = CommandSpec(
        programName = "demo",
        description = "A demo command",
        version = "1.2.3",
        params = listOf(
            OptionSpec(names = listOf("--host"), converter = StringConverter, help = "Bind host", default = "0.0.0.0"),
            OptionSpec(names = listOf("--port", "-p"), converter = IntConverter, help = "Port", required = true),
            OptionSpec(names = listOf("--tag", "-t"), converter = StringConverter, help = "Tags", multiple = true),
            OptionSpec(names = listOf("--color"), converter = enumConverter<Color>(), help = "Color", default = Color.RED),
            FlagSpec(names = listOf("--verbose", "-v"), help = "Verbose"),
            ArgumentSpec(name = "src", converter = StringConverter, help = "Source"),
            ArgumentSpec(name = "files", converter = StringConverter, help = "Files", multiple = true, required = false),
        ),
    )

    private fun parse(vararg argv: String): ParsedValues = ArgotEngine.parse(fullSpec(), arrayOf(*argv))

    @Test
    fun longSpaceForm() {
        val v = parse("--port", "8080", "--host", "example.com", "main.kt")
        assertEquals(8080, v.value("--port"))
        assertEquals("example.com", v.value("--host"))
        assertEquals("main.kt", v.value("src"))
    }

    @Test
    fun longEqualsForm() {
        val v = parse("--port=8080", "--host=example.com", "main.kt")
        assertEquals(8080, v.value("--port"))
        assertEquals("example.com", v.value("--host"))
    }

    @Test
    fun shortSpaceForm() {
        val v = parse("-p", "8080", "main.kt")
        assertEquals(8080, v.value("--port"))
    }

    @Test
    fun shortEqualsForm() {
        val v = parse("-p=8080", "main.kt")
        assertEquals(8080, v.value("--port"))
    }

    @Test
    fun shortAttachedValue() {
        val v = parse("-p8080", "main.kt")
        assertEquals(8080, v.value("--port"))
    }

    @Test
    fun combinedShortFlagThenOption() {
        // `-vp 8080`: -v is a flag, -p is an option that takes the next token.
        val v = parse("-vp", "8080", "main.kt")
        assertTrue(v.flag("--verbose"))
        assertEquals(8080, v.value("--port"))
    }

    @Test
    fun doubleDashTerminator() {
        // Everything after `--` is positional, even option-looking tokens.
        val v = parse("--port", "8080", "--", "--host", "weird")
        assertEquals("0.0.0.0", v.value("--host")) // still the default
        assertEquals("--host", v.value("src"))
        assertEquals(listOf("weird"), v.list<String>("files"))
    }

    @Test
    fun multipleOptionAccumulates() {
        val v = parse("--port", "8080", "-t", "a", "--tag", "b", "main.kt")
        assertEquals(listOf("a", "b"), v.list<String>("--tag"))
    }

    @Test
    fun trailingPositionalList() {
        val v = parse("--port", "8080", "src.kt", "a.txt", "b.txt")
        assertEquals("src.kt", v.value("src"))
        assertEquals(listOf("a.txt", "b.txt"), v.list<String>("files"))
    }

    @Test
    fun enumParsingIsCaseInsensitive() {
        val v = parse("--port", "8080", "--color", "blue", "main.kt")
        assertEquals(Color.BLUE, v.value("--color"))
    }

    @Test
    fun flagPresence() {
        val v = parse("--port", "8080", "-v", "main.kt")
        assertTrue(v.flag("--verbose"))
    }

    @Test
    fun defaultsAppliedWhenAbsent() {
        val v = parse("--port", "8080", "main.kt")
        assertEquals("0.0.0.0", v.value("--host"))
        assertEquals(Color.RED, v.value("--color"))
        assertEquals(false, v.flag("--verbose"))
        assertEquals(emptyList(), v.list<String>("--tag"))
        assertEquals(emptyList(), v.list<String>("files"))
    }

    @Test
    fun helpSignalOnLongAndShort() {
        val help = assertFailsWith<HelpRequested> { parse("--help") }
        assertTrue("Usage:" in help.rendered)
        assertFailsWith<HelpRequested> { parse("-h") }
        // Help is recognized even inside a flag cluster.
        assertFailsWith<HelpRequested> { parse("-vh") }
    }

    @Test
    fun versionSignal() {
        val version = assertFailsWith<VersionRequested> { parse("--version") }
        assertEquals("demo 1.2.3", version.rendered)
    }

    @Test
    fun flagNegation() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(
                FlagSpec(names = listOf("--color"), negationNames = listOf("--no-color"), default = true),
            ),
        )
        assertEquals(true, ArgotEngine.parse(spec, arrayOf()).flag("--color"))
        assertEquals(true, ArgotEngine.parse(spec, arrayOf("--color")).flag("--color"))
        assertEquals(false, ArgotEngine.parse(spec, arrayOf("--no-color")).flag("--color"))
    }
}
