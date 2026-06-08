package org.draftcode.argot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private enum class Mode { FAST, SAFE }

/** A representative command declared with the delegate style, exercising every builder shape. */
private class ServerArgs : Arguments(programName = "serve", description = "Run the server", version = "9.9") {
    val host: String by option("--host", help = "Bind host").default("0.0.0.0")
    val port: Int by option("--port", "-p", help = "Port").int().required()
    val mode: Mode by option("--mode", help = "Mode").enum<Mode>().default(Mode.SAFE)
    val tags: List<String> by option("--tag", "-t", help = "Tags").multiple()
    val verbose: Boolean by flag("--verbose", "-v", help = "Verbose")
    val retries: Int? by option("--retries", help = "Retries").int()
    val src: String by argument(help = "Source")
    val files: List<String> by argument(help = "Files").multiple()
}

class DelegateStyleTest {

    @Test
    fun parsesFullCommandLine() {
        val args = ServerArgs().parse(
            arrayOf(
                "--host", "example.com", "-p", "8080", "--mode", "fast",
                "-t", "a", "--tag", "b", "-v", "--retries", "3",
                "src.kt", "x.txt", "y.txt",
            ),
        )
        assertEquals("example.com", args.host)
        assertEquals(8080, args.port)
        assertEquals(Mode.FAST, args.mode)
        assertEquals(listOf("a", "b"), args.tags)
        assertTrue(args.verbose)
        assertEquals(3, args.retries)
        assertEquals("src.kt", args.src)
        assertEquals(listOf("x.txt", "y.txt"), args.files)
    }

    @Test
    fun appliesDefaultsWhenMinimal() {
        val args = ServerArgs().parse(arrayOf("-p", "80", "only.kt"))
        assertEquals("0.0.0.0", args.host)
        assertEquals(Mode.SAFE, args.mode)
        assertEquals(emptyList(), args.tags)
        assertFalse(args.verbose)
        assertNull(args.retries)
        assertEquals("only.kt", args.src)
        assertEquals(emptyList(), args.files)
    }

    @Test
    fun readBeforeParseFailsFast() {
        val args = ServerArgs()
        val e = assertFailsWith<IllegalStateException> { args.port }
        assertTrue("parsed" in e.message!!, "expected a fail-fast message mentioning parsing, got: ${e.message}")
    }

    @Test
    fun helpSignalPropagatesFromPureParse() {
        assertFailsWith<HelpRequested> { ServerArgs().parse(arrayOf("--help")) }
    }

    @Test
    fun missingRequiredOptionPropagates() {
        assertFailsWith<ArgotParseException.MissingRequiredOption> { ServerArgs().parse(arrayOf("src.kt")) }
    }

    @Test
    fun parseReturnsSameTypedInstance() {
        val original = ServerArgs()
        val returned = original.parse(arrayOf("-p", "1", "s"))
        assertSame(original, returned)
    }
}
