package org.draftcode.argot.dokkajson

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CodeBlock
import org.jetbrains.dokka.model.doc.Text

class JsonTest {

    @Test
    fun `escapes characters that would break a JavaScript module`() {
        // U+2028 is legal inside a JSON string but ends a line in JavaScript.
        val rendered = JsonString("a\u2028b\u2029c").render()
        assertEquals("\"a\\u2028b\\u2029c\"", rendered)
    }

    @Test
    fun `escapes quotes, backslashes and control characters`() {
        assertEquals("\"\\\"\"", JsonString("\"").render())
        assertEquals("\"\\\\\"", JsonString("\\").render())
        assertEquals("\"\\n\"", JsonString("\n").render())
        assertEquals("\"\\t\"", JsonString("\t").render())
        assertEquals("\"\\u0000\"", JsonString("\u0000").render())
    }

    @Test
    fun `omits null entries rather than emitting them`() {
        val obj = jsonObject("kept" to "yes".json(), "dropped" to null)
        assertEquals("""
            {
              "kept": "yes"
            }
        """.trimIndent(), obj.render())
    }

    @Test
    fun `omits empty collections entirely`() {
        assertEquals(null, emptyList<String>().jsonOrNull { it.json() })
        assertEquals("""
            [
              "a"
            ]
        """.trimIndent(), listOf("a").jsonOrNull { it.json() }?.render())
    }
}

class IdsTest {

    @Test
    fun `leaves unique ids untouched`() {
        assertEquals(listOf("a", "b"), disambiguate(listOf("a", "b")))
    }

    @Test
    fun `suffixes overloads so each keeps its own anchor`() {
        assertEquals(
            listOf("f~0", "g", "f~1", "f~2"),
            disambiguate(listOf("f", "g", "f", "f")),
        )
    }
}

class DocsTest {
    private val docs = DocRenderer(ownPackages = setOf("org.draftcode.argot"))

    @Test
    fun `a fenced block keeps one line per line`() {
        // Dokka splits a fenced block into one Text per line joined by Br rather than embedding
        // newlines, so a renderer that only concatenates bodies silently produces a single line.
        val block = CodeBlock(listOf(Text("val a = 1"), Br, Text("    val b = 2")))
        val html = docs.render(block)
        assertTrue("val a = 1\n    val b = 2" in html, html)
    }

    @Test
    fun `a fenced block escapes markup rather than emitting it`() {
        val block = CodeBlock(listOf(Text("Converter<String>")))
        assertTrue("Converter&lt;String&gt;" in docs.render(block))
    }
}
