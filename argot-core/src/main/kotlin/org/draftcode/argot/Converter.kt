package org.draftcode.argot

/**
 * Converts a raw command-line string into a typed value.
 *
 * A converter is the single extension point for adding new parameter types. Implementations must
 * be pure (no I/O, no global state) and should throw [ArgotConversionException] when [convert]
 * cannot interpret the input; the engine catches that and reports a user-facing
 * [ArgotParseException.InvalidValue] enriched with the parameter name.
 *
 * Example — a converter for a comma-free, lowercase identifier:
 * ```
 * object SlugConverter : Converter<String> {
 *     override val typeName: String = "slug"
 *     override fun convert(raw: String): String {
 *         require(raw.all { it.isLetterOrDigit() || it == '-' }) { "not a slug: '$raw'" }
 *         return raw.lowercase()
 *     }
 * }
 * ```
 *
 * @param T the produced value type.
 */
public interface Converter<out T> {
    /**
     * A short, human-readable name for the target type (for example `"Int"` or `"slug"`).
     *
     * Used in help text and in [ArgotParseException.InvalidValue] messages. Defaults to a generic
     * label so simple custom converters need not override it.
     */
    public val typeName: String get() = "value"

    /**
     * Parses [raw] into a value of type [T].
     *
     * @throws ArgotConversionException if [raw] cannot be converted. Any other exception thrown
     *   here is also caught by the engine and surfaced as [ArgotParseException.InvalidValue].
     */
    public fun convert(raw: String): T
}

/**
 * Thrown by a [Converter] when a raw string cannot be converted to the target type.
 *
 * Consumers rarely catch this directly: the engine translates it into a user-facing
 * [ArgotParseException.InvalidValue] that also carries the offending parameter's name.
 */
public class ArgotConversionException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
