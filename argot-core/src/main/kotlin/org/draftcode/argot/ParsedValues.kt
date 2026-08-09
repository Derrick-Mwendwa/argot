package org.draftcode.argot

/** The typed result of a successful parse, keyed by each parameter's canonical name. */
public class ParsedValues internal constructor(
    private val values: Map<String, Any?>,
) {
    /**
     * Returns the value bound to [name], cast to [T]. For required or defaulted parameters, where a
     * value is always present.
     *
     * @throws IllegalArgumentException if no parameter named [name] was part of the parse.
     */
    public fun <T> value(name: String): T {
        require(values.containsKey(name)) { "no parameter named '$name' was parsed" }
        @Suppress("UNCHECKED_CAST")
        return values[name] as T
    }

    /**
     * Returns the value bound to [name] cast to [T], or `null` if it was not supplied.
     *
     * `null` means "declared but absent", never "no such parameter" — a name that was not part of
     * the parse is a mistake rather than a missing value.
     *
     * @throws IllegalArgumentException if no parameter named [name] was part of the parse.
     */
    public fun <T> valueOrNull(name: String): T? {
        require(values.containsKey(name)) { "no parameter named '$name' was parsed" }
        @Suppress("UNCHECKED_CAST")
        return values[name] as T?
    }

    /**
     * Returns the state of the flag [name] (its default if it was never supplied).
     *
     * @throws IllegalArgumentException if no flag named [name] was part of the parse.
     */
    public fun flag(name: String): Boolean {
        require(values.containsKey(name)) { "no parameter named '$name' was parsed" }
        val value = values[name]
        require(value is Boolean) { "'$name' is not a flag" }
        return value
    }

    /**
     * Returns the accumulated list bound to a `multiple` parameter [name], empty when it was never
     * supplied.
     *
     * @throws IllegalArgumentException if [name] was not part of the parse, or is not `multiple`.
     */
    public fun <T> list(name: String): List<T> {
        require(values.containsKey(name)) { "no parameter named '$name' was parsed" }
        val value = values[name]
        require(value is List<*>) { "'$name' is not a 'multiple' parameter" }
        @Suppress("UNCHECKED_CAST")
        return value as List<T>
    }
}
