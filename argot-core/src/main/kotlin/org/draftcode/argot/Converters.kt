package org.draftcode.argot

/**
 * Built-in [Converter] for [String] values. Returns the raw input unchanged.
 *
 * Referenced by name in generated code, so its name is part of the public API.
 */
public object StringConverter : Converter<String> {
    override val typeName: String = "String"
    override fun convert(raw: String): String = raw
}

/** Built-in [Converter] for [Int] values (base 10). */
public object IntConverter : Converter<Int> {
    override val typeName: String = "Int"
    override fun convert(raw: String): Int =
        raw.toIntOrNull() ?: throw ArgotConversionException("'$raw' is not a valid Int")
}

/** Built-in [Converter] for [Long] values (base 10). */
public object LongConverter : Converter<Long> {
    override val typeName: String = "Long"
    override fun convert(raw: String): Long =
        raw.toLongOrNull() ?: throw ArgotConversionException("'$raw' is not a valid Long")
}

/** Built-in [Converter] for [Double] values. */
public object DoubleConverter : Converter<Double> {
    override val typeName: String = "Double"
    override fun convert(raw: String): Double =
        raw.toDoubleOrNull() ?: throw ArgotConversionException("'$raw' is not a valid Double")
}

/**
 * Built-in [Converter] for [Boolean] *option values* (not flags).
 *
 * Accepts, case-insensitively, `true/false`, `1/0`, `yes/no`, `y/n`, `on/off`. Flags use
 * presence semantics instead and do not go through this converter for their default behavior.
 */
public object BooleanConverter : Converter<Boolean> {
    override val typeName: String = "Boolean"

    private val truthy = setOf("true", "1", "yes", "y", "on")
    private val falsy = setOf("false", "0", "no", "n", "off")

    override fun convert(raw: String): Boolean {
        val normalized = raw.lowercase()
        return when (normalized) {
            in truthy -> true
            in falsy -> false
            else -> throw ArgotConversionException("'$raw' is not a valid Boolean (expected true/false)")
        }
    }
}

/**
 * Built-in [Converter] for an enum type, matching a raw string against enum constant names
 * (case-insensitively).
 *
 * Constructed without reflection: callers supply the constant list explicitly. The
 * [enumConverter] factory builds one from a reified type parameter.
 *
 * @param entries the enum's constants, in declaration order.
 * @param typeName the enum's simple name, used in help and error messages.
 */
public class EnumConverter<out E : Enum<*>>(
    private val entries: List<E>,
    override val typeName: String,
) : Converter<E> {
    override fun convert(raw: String): E =
        entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            ?: throw ArgotConversionException(
                "'$raw' is not a valid $typeName (expected one of ${entries.joinToString(", ") { it.name }})",
            )
}

/**
 * Creates an [EnumConverter] for the reified enum type [E].
 *
 * Uses [enumValues] and `simpleName`, both of which are served by the standard library and do
 * **not** require `kotlin-reflect`.
 *
 * Example:
 * ```
 * enum class Mode { FAST, SAFE }
 * val converter = enumConverter<Mode>()
 * ```
 */
public inline fun <reified E : Enum<E>> enumConverter(): EnumConverter<E> =
    EnumConverter(enumValues<E>().toList(), E::class.simpleName ?: "enum")
