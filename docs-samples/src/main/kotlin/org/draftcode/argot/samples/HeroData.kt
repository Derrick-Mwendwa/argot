package org.draftcode.argot.samples

import java.io.File
import org.draftcode.argot.HelpRequested
import org.draftcode.argot.parse

/**
 * Emits the landing page hero's three panels: the same `greet` program declared with delegates and
 * with annotations, and the help screen Argot generates from it.
 *
 * Every panel is real — the declarations are read from the sample sources, the terminal output comes
 * from running the parser — so the page cannot claim behaviour the library does not have.
 */
fun main(args: Array<String>) {
    val delegates = File(args[0])
    val annotations = File(args[1])
    val version = args[2]
    val output = File(args[3])

    val json = buildString {
        append("{\n")
        append("  \"version\": ").append(quote(version)).append(",\n")
        append("  \"install\": ")
            .append(quote("implementation(\"org.draftcode:argot-core:$version\")"))
            .append(",\n")
        append("  \"panels\": [\n")
        append(
            codePanel("delegates", "Delegates", "GreetArgs.kt", delegates.readText().region("declare"))
        )
        append(",\n")
        append(
            codePanel(
                "annotations",
                "Annotations",
                "GreetCommand.kt",
                annotations.readText().region("declare"),
            )
        )
        append(",\n")
        append(terminalPanel())
        append("\n  ]\n}\n")
    }

    output.parentFile.mkdirs()
    output.writeText(json)
}

private fun codePanel(id: String, label: String, file: String, code: String): String = buildString {
    append("    {")
    append("\"id\": ").append(quote(id)).append(", ")
    append("\"label\": ").append(quote(label)).append(", ")
    append("\"kind\": \"code\", ")
    append("\"file\": ").append(quote(file)).append(", ")
    append("\"code\": ").append(quote(code))
    append("}")
}

/** The `--help` output, obtained the only way a user can: by asking for it. */
private fun terminalPanel(): String {
    val rendered =
        try {
            GreetArgs().parse(arrayOf("--help"))
            error("--help did not raise HelpRequested; the sample or the library has changed")
        } catch (e: HelpRequested) {
            e.rendered.trimEnd()
        }
    return buildString {
        append("    {")
        append("\"id\": \"terminal\", ")
        append("\"label\": \"Terminal\", ")
        append("\"kind\": \"terminal\", ")
        append("\"file\": \"greet — zsh\", ")
        append("\"command\": \"greet --help\", ")
        append("\"output\": ").append(quote(rendered))
        append("}")
    }
}

private fun String.region(name: String): String {
    val start = indexOf("// #region $name")
    val end = indexOf("// #endregion $name")
    require(start != -1 && end != -1) { "no region '$name' in the sample source" }
    val afterMarker = indexOf('\n', start).let { if (it == -1) start else it + 1 }
    return substring(afterMarker, end).trim('\n').trimEnd()
}

private fun quote(text: String): String = buildString {
    append('"')
    for (ch in text) {
        when {
            ch == '"' -> append("\\\"")
            ch == '\\' -> append("\\\\")
            ch == '\n' -> append("\\n")
            ch == '\t' -> append("\\t")
            ch.code < 0x20 -> append("\\u").append(ch.code.toString(16).padStart(4, '0'))
            else -> append(ch)
        }
    }
    append('"')
}
