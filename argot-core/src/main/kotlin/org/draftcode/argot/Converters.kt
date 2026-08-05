package org.draftcode.argot

public object StringConverter : Converter<String> {
    override val typeName: String = "String"
    override fun convert(raw: String): String = raw
}

public object IntConverter : Converter<Int> {
    override val typeName: String = "Int"
    override fun convert(raw: String): Int =
        raw.toIntOrNull() ?: throw ArgotConversionException("'$raw' is not a valid Int")
}

public object LongConverter : Converter<Long> {
    override val typeName: String = "Long"
    override fun convert(raw: String): Long =
        raw.toLongOrNull() ?: throw ArgotConversionException("'$raw' is not a valid Long")
}

public object DoubleConverter : Converter<Double> {
    override val typeName: String = "Double"
    override fun convert(raw: String): Double =
        raw.toDoubleOrNull() ?: throw ArgotConversionException("'$raw' is not a valid Double")
}

/** Accepts, case-insensitively, `true/false`, `1/0`, `yes/no`, `y/n`, and `on/off`. */
public object BooleanConverter : Converter<Boolean> {
    override val typeName: String = "Boolean"

    private val truthy = setOf("true", "1", "yes", "y", "on")
    private val falsy = setOf("false", "0", "no", "n", "off")

    override fun convert(raw: String): Boolean = when (raw.lowercase()) {
        in truthy -> true
        in falsy -> false
        else -> throw ArgotConversionException("'$raw' is not a valid Boolean (expected true/false)")
    }
}

/**
 * Matches a raw value against enum constant names, case-insensitively.
 *
 * @param entries the enum's constants.
 * @param typeName the enum's name, shown in help and error messages.
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

public inline fun <reified E : Enum<E>> enumConverter(): EnumConverter<E> =
    EnumConverter(enumValues<E>().toList(), E::class.simpleName ?: "enum")
