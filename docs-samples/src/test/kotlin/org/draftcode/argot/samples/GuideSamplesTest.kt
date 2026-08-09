package org.draftcode.argot.samples

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.draftcode.argot.ArgotParseException
import org.draftcode.argot.HelpRequested
import org.draftcode.argot.parse

/**
 * Asserts the output the how-to guides claim. As with [SamplesTest], a sample that compiles but
 * behaves differently from the prose is still a broken doc.
 */
class GuideSamplesTest {

    @Test
    fun `a repeated option collects every occurrence in order`() {
        val args = TagArgs().parse(arrayOf("--tag", "beta", "-t", "eu", "--env", "staging"))
        assertEquals("tags=beta,eu envs=staging", summarise(args))
    }

    @Test
    fun `an absent repeated option is an empty list, not null`() {
        val args = TagArgs().parse(arrayOf("--env", "prod"))
        assertEquals(emptyList(), args.tag)
    }

    @Test
    fun `required on a repeated option demands at least one`() {
        assertFailsWith<ArgotParseException> { TagArgs().parse(arrayOf("--tag", "beta")) }
    }

    @Test
    fun `a repeated option converts every value`() {
        val args = PortArgs().parse(arrayOf("-p", "80", "-p", "443"))
        assertEquals(listOf(80, 443), args.port)
    }

    @Test
    fun `a custom converter produces a typed value`() {
        assertEquals(Duration(300), TimeoutArgs().parse(arrayOf("--timeout", "5m")).timeout)
        assertEquals(Duration(30), TimeoutArgs().parse(emptyArray()).timeout)
    }

    // The converter's own message is currently discarded by ArgotEngine.convert, which reports
    // only the type name. The guide says so rather than promising a message users will not see.
    @Test
    fun `a rejected value is reported with the converter's type name`() {
        val failure =
            assertFailsWith<ArgotParseException> { TimeoutArgs().parse(arrayOf("-t", "soon")) }
        assertEquals("invalid value 'soon' for -t (expected Duration)", failure.message)
    }

    @Test
    fun `a custom converter names the type in help`() {
        val help = assertFailsWith<HelpRequested> { TimeoutArgs().parse(arrayOf("--help")) }.rendered
        assertTrue("<Duration>" in help, help)
    }

    @Test
    fun `an optional positional is null when absent`() {
        assertNull(CopyArgs().parse(arrayOf("notes.txt")).destination)
        assertEquals("notes.txt.bak", destinationOf(CopyArgs().parse(arrayOf("notes.txt"))))
    }

    @Test
    fun `a required positional is still required`() {
        assertFailsWith<ArgotParseException> { CopyArgs().parse(emptyArray()) }
    }

    @Test
    fun `a trailing positional captures the rest`() {
        val args = ArchiveArgs().parse(arrayOf("out.zip", "a.txt", "b.txt", "c.txt"))
        assertEquals("out.zip", args.output)
        assertEquals(listOf("a.txt", "b.txt", "c.txt"), args.inputs)
    }

    @Test
    fun `an enum option matches regardless of case`() {
        assertEquals(Level.WARN, LogArgs().parse(arrayOf("--level", "warn")).level)
        assertEquals(Level.WARN, LogArgs().parse(arrayOf("--level", "WARN")).level)
        assertEquals(Level.INFO, LogArgs().parse(emptyArray()).level)
    }

    @Test
    fun `an enum option names the type when given a bad value`() {
        val failure =
            assertFailsWith<ArgotParseException> { LogArgs().parse(arrayOf("--level", "loud")) }
        assertEquals("invalid value 'loud' for --level (expected Level)", failure.message)
    }

    @Test
    fun `filtering by the parsed level keeps that level and above`() {
        assertEquals(
            listOf(Level.WARN, Level.ERROR),
            atLeast(LogArgs().parse(arrayOf("-l", "warn"))),
        )
    }
}

/**
 * The annotation-style tutorial claims its `greet` behaves exactly like the delegate-style one from
 * [SamplesTest]. These assert the same inputs produce the same outputs through the generated parser.
 */
class AnnotationTutorialTest {

    @Test
    fun `defaults match the delegate style`() {
        assertEquals(listOf("Hello, world!"), greetings(parseGreetCommand(emptyArray())))
    }

    @Test
    fun `name, count and loud match the delegate style`() {
        val args = parseGreetCommand(arrayOf("--name", "Ada", "--count", "2", "--loud"))
        assertEquals(listOf("HELLO, ADA!", "HELLO, ADA!"), greetings(args))
    }

    @Test
    fun `short names work the same way`() {
        assertEquals(3, greetings(parseGreetCommand(arrayOf("-n", "Ada", "-c", "3"))).size)
    }

    @Test
    fun `the generated parser renders the same help as the delegate style`() {
        val generated =
            assertFailsWith<HelpRequested> { parseGreetCommand(arrayOf("--help")) }.rendered
        val delegated = assertFailsWith<HelpRequested> { GreetArgs().parse(arrayOf("--help")) }.rendered
        assertEquals(delegated, generated)
    }

    @Test
    fun `a bad value is rejected the same way`() {
        val failure =
            assertFailsWith<ArgotParseException> { parseGreetCommand(arrayOf("--count", "banana")) }
        assertEquals("invalid value 'banana' for --count (expected Int)", failure.message)
    }
}
