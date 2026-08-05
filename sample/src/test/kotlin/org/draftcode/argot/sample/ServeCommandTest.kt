package org.draftcode.argot.sample

import org.draftcode.argot.ArgotParseException
import org.draftcode.argot.HelpRequested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Calls the generated `parseServeArgs`, exercising the KSP processor end to end. */
class ServeCommandTest {

    @Test
    fun parsesWithDefaultsAndPositionalList() {
        val args = parseServeArgs(arrayOf("--port", "8080", "a.txt", "b.txt"))
        assertEquals("0.0.0.0", args.host) // annotation default
        assertEquals(8080, args.port)
        assertFalse(args.verbose)
        assertEquals(listOf("a.txt", "b.txt"), args.files)
        assertEquals("serving [a.txt, b.txt] on 0.0.0.0:8080 (verbose=false)", serveSummary(args))
    }

    @Test
    fun parsesAllOptions() {
        val args = parseServeArgs(arrayOf("--host", "0.0.0.0", "-p", "9090", "-v"))
        assertEquals("0.0.0.0", args.host)
        assertEquals(9090, args.port)
        assertTrue(args.verbose)
        assertEquals(emptyList(), args.files)
    }

    @Test
    fun helpCaseSignalsHelp() {
        assertFailsWith<HelpRequested> { parseServeArgs(arrayOf("--help")) }
    }

    @Test
    fun errorCaseMissingRequiredPort() {
        assertFailsWith<ArgotParseException.MissingRequiredOption> { parseServeArgs(arrayOf("a.txt")) }
    }

    @Test
    fun errorCaseInvalidPort() {
        assertFailsWith<ArgotParseException.InvalidValue> { parseServeArgs(arrayOf("--port", "notanint")) }
    }
}
