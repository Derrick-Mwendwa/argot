package org.draftcode.argot.dokkajson

/**
 * A minimal JSON writer, hand-rolled because this module is loaded into Dokka's worker classpath
 * where every added dependency risks colliding with one Dokka already supplies.
 */
public sealed interface Json {
    public fun write(out: StringBuilder, indent: Int)
}

public data class JsonString(val value: String) : Json {
    override fun write(out: StringBuilder, indent: Int) {
        out.append('"')
        for (ch in value) {
            when {
                ch == '"' -> out.append("\\\"")
                ch == '\\' -> out.append("\\\\")
                ch == '\n' -> out.append("\\n")
                ch == '\r' -> out.append("\\r")
                ch == '\t' -> out.append("\\t")
                // U+2028/U+2029 are legal in a JSON string but terminate a line in JavaScript, and
                // the site imports this output as a module.
                ch.code < 0x20 || ch.code == 0x2028 || ch.code == 0x2029 ->
                    out.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                else -> out.append(ch)
            }
        }
        out.append('"')
    }
}

public data class JsonBool(val value: Boolean) : Json {
    override fun write(out: StringBuilder, indent: Int) {
        out.append(if (value) "true" else "false")
    }
}

public data class JsonNumber(val value: Long) : Json {
    override fun write(out: StringBuilder, indent: Int) {
        out.append(value)
    }
}

public data object JsonNull : Json {
    override fun write(out: StringBuilder, indent: Int) {
        out.append("null")
    }
}

public class JsonArray(private val items: List<Json>) : Json {
    override fun write(out: StringBuilder, indent: Int) {
        if (items.isEmpty()) {
            out.append("[]")
            return
        }
        out.append("[\n")
        items.forEachIndexed { i, item ->
            pad(out, indent + 1)
            item.write(out, indent + 1)
            if (i < items.size - 1) out.append(',')
            out.append('\n')
        }
        pad(out, indent)
        out.append(']')
    }
}

public class JsonObject(private val entries: List<Pair<String, Json>>) : Json {
    override fun write(out: StringBuilder, indent: Int) {
        if (entries.isEmpty()) {
            out.append("{}")
            return
        }
        out.append("{\n")
        entries.forEachIndexed { i, (key, value) ->
            pad(out, indent + 1)
            JsonString(key).write(out, indent + 1)
            out.append(": ")
            value.write(out, indent + 1)
            if (i < entries.size - 1) out.append(',')
            out.append('\n')
        }
        pad(out, indent)
        out.append('}')
    }
}

private fun pad(out: StringBuilder, indent: Int) {
    repeat(indent) { out.append("  ") }
}

public fun Json.render(): String = StringBuilder().also { write(it, 0) }.toString()

/** Builds an object, dropping entries whose value is null so the output stays free of noise. */
public fun jsonObject(vararg entries: Pair<String, Json?>): JsonObject =
    JsonObject(entries.mapNotNull { (k, v) -> v?.let { k to it } })

public fun String.json(): JsonString = JsonString(this)

public fun Boolean.json(): JsonBool = JsonBool(this)

public fun Int.json(): JsonNumber = JsonNumber(toLong())

/** Omits the key entirely when the collection is empty, rather than emitting `[]` everywhere. */
public fun <T> List<T>.jsonOrNull(transform: (T) -> Json): JsonArray? =
    if (isEmpty()) null else JsonArray(map(transform))
