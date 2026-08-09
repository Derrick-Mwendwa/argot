package org.draftcode.argot.dokkajson

import org.jetbrains.dokka.model.doc.A
import org.jetbrains.dokka.model.doc.B
import org.jetbrains.dokka.model.doc.BlockQuote
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CodeBlock
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.CustomDocTag
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.Em
import org.jetbrains.dokka.model.doc.Header
import org.jetbrains.dokka.model.doc.HorizontalRule
import org.jetbrains.dokka.model.doc.I
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Ol
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Pre
import org.jetbrains.dokka.model.doc.Strikethrough
import org.jetbrains.dokka.model.doc.Strong
import org.jetbrains.dokka.model.doc.Table
import org.jetbrains.dokka.model.doc.Td
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Th
import org.jetbrains.dokka.model.doc.Tr
import org.jetbrains.dokka.model.doc.Ul
import org.jetbrains.dokka.model.doc.Var

/**
 * Renders Dokka's parsed KDoc tree into HTML.
 *
 * Links to the library's own declarations are emitted as `<a data-ref="ID">` with no `href`; the
 * site resolves those once it knows its own routing. A `[link]` to anything else renders as `<code>`
 * rather than a dead anchor.
 */
internal class DocRenderer(private val ownPackages: Set<String>) {

    fun render(tag: DocTag): String = buildString { appendTag(tag) }

    /** Plain text, for search indexes and summaries. */
    fun plainText(tag: DocTag): String =
        buildString { appendText(tag) }.replace(WHITESPACE, " ").trim()

    private fun StringBuilder.appendText(tag: DocTag) {
        if (tag is Text) append(tag.body)
        tag.children.forEach { appendText(it) }
    }

    /**
     * Text of a code block, keeping its line structure.
     *
     * Dokka splits a fenced block into one [Text] per line joined by [Br], so [appendText] — which
     * only concatenates bodies — collapses the whole sample onto a single line.
     */
    private fun StringBuilder.appendCode(tag: DocTag) {
        when (tag) {
            is Text -> append(tag.body)
            is Br -> append('\n')
            is P -> {
                tag.children.forEach { appendCode(it) }
                append('\n')
            }
            else -> tag.children.forEach { appendCode(it) }
        }
    }

    // Named appendTag rather than append: an `append(DocTag)` extension loses overload resolution
    // to StringBuilder's own append(Any?), which silently emits the tag's toString().
    private fun StringBuilder.appendTag(tag: DocTag) {
        when (tag) {
            is Text -> append(escape(tag.body))
            is P -> wrap("p", tag)
            is B, is Strong -> wrap("strong", tag)
            is I, is Em -> wrap("em", tag)
            is Strikethrough -> wrap("s", tag)
            is CodeInline -> wrap("code", tag)
            is Var -> wrap("var", tag)
            is Ul -> wrap("ul", tag)
            is Ol -> wrap("ol", tag)
            is Li -> wrap("li", tag)
            is BlockQuote -> wrap("blockquote", tag)
            is Table -> wrap("table", tag)
            is Tr -> wrap("tr", tag)
            is Td -> wrap("td", tag)
            is Th -> wrap("th", tag)
            is Br -> append("<br>")
            is HorizontalRule -> append("<hr>")

            // Demoted: the page already spends h1 on the declaration and h2 on section titles.
            is Header -> {
                val level = (tag.params["level"]?.toIntOrNull() ?: 1).coerceIn(1, 4)
                wrap("h${level + 2}", tag)
            }

            is CodeBlock, is Pre -> {
                val lang = tag.params["lang"] ?: "kotlin"
                append("<pre><code class=\"language-").append(escape(lang)).append("\">")
                append(escape(buildString { appendCode(tag) }))
                append("</code></pre>")
            }

            is DocumentationLink -> {
                val label = buildString { tag.children.forEach { appendTag(it) } }
                if (tag.dri.isLocal(ownPackages) && tag.dri.pointsToDeclaration()) {
                    append("<a data-ref=\"").append(escape(tag.dri.id())).append("\">")
                    append(label)
                    append("</a>")
                } else {
                    append("<code>").append(label).append("</code>")
                }
            }

            is A -> {
                val href = tag.params["href"]
                if (href != null) {
                    append("<a href=\"").append(escape(href)).append("\">")
                    tag.children.forEach { appendTag(it) }
                    append("</a>")
                } else {
                    tag.children.forEach { appendTag(it) }
                }
            }

            // The root of a parsed KDoc block, and any tag without a case: containers only.
            is CustomDocTag -> tag.children.forEach { appendTag(it) }
            else -> tag.children.forEach { appendTag(it) }
        }
    }

    private fun StringBuilder.wrap(element: String, tag: DocTag) {
        append('<').append(element).append('>')
        tag.children.forEach { appendTag(it) }
        append("</").append(element).append('>')
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}

internal fun escape(text: String): String = buildString {
    for (ch in text) {
        when (ch) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            else -> append(ch)
        }
    }
}
