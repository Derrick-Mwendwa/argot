package org.draftcode.argot

/**
 * Converts a raw command-line string into a typed value. Implement this to support a type beyond
 * the built-ins.
 *
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
    /** A short name for the target type (for example `"Int"`), shown in help and error messages. */
    public val typeName: String get() = "value"

    /**
     * Parses [raw] into a value of type [T].
     *
     * @throws ArgotConversionException if [raw] cannot be converted.
     */
    public fun convert(raw: String): T
}

/** Thrown by a [Converter] when a raw string cannot be converted to the target type. */
public class ArgotConversionException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
