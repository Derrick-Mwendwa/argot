package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Verifies deterministic, aligned usage and `--help` rendering (§4.6). */
class RenderTest {

    private fun fullSpec() = CommandSpec(
        programName = "demo",
        description = "A demo command",
        version = "1.2.3",
        params = listOf(
            OptionSpec(names = listOf("--host"), converter = StringConverter, help = "Bind host", default = "0.0.0.0"),
            OptionSpec(names = listOf("--port", "-p"), converter = IntConverter, help = "Port", required = true),
            FlagSpec(names = listOf("--verbose", "-v"), help = "Verbose"),
            ArgumentSpec(name = "src", converter = StringConverter, help = "Source file"),
            ArgumentSpec(name = "files", converter = StringConverter, help = "Extra files", multiple = true, required = false),
        ),
    )

    @Test
    fun usageLine() {
        assertEquals("Usage: demo [options] <src> [<files>...]", fullSpec().renderUsage())
    }

    @Test
    fun helpIsDeterministicAndComplete() {
        val help = fullSpec().renderHelp()
        assertTrue(help.startsWith("A demo command"))
        assertTrue("Usage: demo [options] <src> [<files>...]" in help)
        assertTrue("Options:" in help)
        assertTrue("--host <String>" in help)
        assertTrue("(default: 0.0.0.0)" in help)
        assertTrue("--port, -p <Int>" in help)
        assertTrue("(required)" in help)
        assertTrue("--verbose, -v" in help)
        assertTrue("-h, --help" in help)
        assertTrue("--version" in help)
        assertTrue("Arguments:" in help)
        assertTrue("<src>" in help)
        assertTrue("Source file" in help)
        assertTrue("<files>" in help)
        // Rendering is deterministic: identical specs produce identical output.
        assertEquals(help, fullSpec().renderHelp())
    }

    @Test
    fun noVersionLineWhenUnset() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(FlagSpec(names = listOf("--verbose"), help = "Verbose")),
        )
        val help = spec.renderHelp()
        assertFalse("--version" in help)
        // No positionals -> no Arguments section.
        assertFalse("Arguments:" in help)
    }
}
