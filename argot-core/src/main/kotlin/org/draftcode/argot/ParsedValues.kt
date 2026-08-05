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

    /** Returns the value bound to [name] cast to [T], or `null` if it was absent. */
    public fun <T> valueOrNull(name: String): T? {
        @Suppress("UNCHECKED_CAST")
        return values[name] as T?
    }

    /** Returns the state of the flag [name] (its default if it was never supplied). */
    public fun flag(name: String): Boolean = (values[name] as? Boolean) ?: false

    /** Returns the accumulated list bound to a `multiple` parameter [name] (empty if absent). */
    public fun <T> list(name: String): List<T> {
        @Suppress("UNCHECKED_CAST")
        return (values[name] as? List<T>) ?: emptyList()
    }
}
