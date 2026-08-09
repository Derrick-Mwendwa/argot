package org.draftcode.argot.dokkajson

import kotlin.test.Test
import kotlin.test.assertEquals

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
