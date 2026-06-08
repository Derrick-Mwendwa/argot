package org.draftcode.argot

/**
 * The typed result of a successful parse.
 *
 * Both consumer styles read from this: generated code and the delegate base class look values up
 * by a parameter's canonical name (an option/flag's first name, or an argument's `name`).
 *
 * Lookups are unchecked casts by design — the engine guarantees that a stored value matches the
 * declaring spec's converter type, so a correctly generated/declared call site is always safe.
 */
public class ParsedValues internal constructor(
    private val values: Map<String, Any?>,
) {
    /**
     * Returns the value bound to [name], cast to [T].
     *
     * Intended for required options/arguments and options with a default, where a non-null value
     * is guaranteed to exist.
     *
     * @throws IllegalArgumentException if no parameter named [name] was part of the parse (a
     *   programming error at the call site, not a user-input error).
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

    /** Returns the boolean state of the flag [name] (its default if it was never supplied). */
    public fun flag(name: String): Boolean = (values[name] as? Boolean) ?: false

    /** Returns the accumulated list bound to a `multiple` option/argument [name] (empty if absent). */
    public fun <T> list(name: String): List<T> {
        @Suppress("UNCHECKED_CAST")
        return (values[name] as? List<T>) ?: emptyList()
    }
}
