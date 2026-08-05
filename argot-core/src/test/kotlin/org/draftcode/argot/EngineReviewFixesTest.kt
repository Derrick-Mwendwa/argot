package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Regression tests: short-name flag negation, `--help`/`--version` written with an attached
 * `=value`, and declaration-order help rendering of interleaved options and flags.
 */
class EngineReviewFixesTest {

    @Test
    fun shortFlagNegationIsParsed() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(FlagSpec(names = listOf("-c"), negationNames = listOf("-C"), default = true)),
        )
        assertEquals(true, ArgotEngine.parse(spec, arrayOf()).flag("-c"))
        assertEquals(false, ArgotEngine.parse(spec, arrayOf("-C")).flag("-c"))
        // Inside a cluster the last token wins: -c sets true, then -C sets false.
        assertEquals(false, ArgotEngine.parse(spec, arrayOf("-cC")).flag("-c"))
    }

    @Test
    fun helpAndVersionRecognizedWithAttachedValue() {
        val spec = CommandSpec(
            programName = "demo",
            version = "1.0",
            params = listOf(OptionSpec(names = listOf("--port"), converter = IntConverter, required = true)),
        )
        assertFailsWith<HelpRequested> { ArgotEngine.parse(spec, arrayOf("--help=x")) }
        assertFailsWith<VersionRequested> { ArgotEngine.parse(spec, arrayOf("--version=x")) }
    }

    @Test
    fun optionsAndFlagsRenderInDeclarationOrder() {
        val spec = CommandSpec(
            programName = "demo",
            params = listOf(
                OptionSpec(names = listOf("--alpha"), converter = StringConverter, help = "A"),
                FlagSpec(names = listOf("--bravo"), help = "B"),
                OptionSpec(names = listOf("--charlie"), converter = StringConverter, help = "C"),
                FlagSpec(names = listOf("--delta"), help = "D"),
            ),
        )
        val help = spec.renderHelp()
        val alpha = help.indexOf("--alpha")
        val bravo = help.indexOf("--bravo")
        val charlie = help.indexOf("--charlie")
        val delta = help.indexOf("--delta")
        assertTrue(
            alpha in 0 until bravo && bravo < charlie && charlie < delta,
            "expected declaration order alpha<bravo<charlie<delta, got $alpha/$bravo/$charlie/$delta",
        )
    }
}
