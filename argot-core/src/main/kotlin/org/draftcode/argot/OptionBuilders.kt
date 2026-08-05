package org.draftcode.argot

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Builds a value-bearing option. The call you end with decides the property's type:
 * - end here and the property is nullable, resolving to `null` when the option is absent;
 * - [required] and [default] give a non-null value;
 * - [multiple] collects repeats into a `List<T>`.
 *
 * Refine the value type (`.int()`, `.enum<E>()`, `.convert(...)`) before choosing cardinality.
 *
 * @param T the option's value type.
 */
public class OptionBuilder<T : Any> internal constructor(
    private val names: List<String>,
    private val help: String,
    private val converter: Converter<T>,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, T?>> {

    public fun <R : Any> convert(converter: Converter<R>): OptionBuilder<R> =
        OptionBuilder(names, help, converter)

    public fun int(): OptionBuilder<Int> = convert(IntConverter)

    public fun long(): OptionBuilder<Long> = convert(LongConverter)

    public fun double(): OptionBuilder<Double> = convert(DoubleConverter)

    /** Parses the value as a [Boolean]: `true/false`, `1/0`, `yes/no`, `y/n`, or `on/off`. */
    public fun boolean(): OptionBuilder<Boolean> = convert(BooleanConverter)

    /** Parses the value as the enum [E], matching constant names case-insensitively. */
    public inline fun <reified E : Enum<E>> enum(): OptionBuilder<E> = convert(enumConverter<E>())

    /** Requires the option; parsing fails when it is absent. */
    public fun required(): RequiredOptionBuilder<T> =
        RequiredOptionBuilder(names, help, converter)

    /** Uses [value] when the option is absent. */
    public fun default(value: T): RequiredOptionBuilder<T> =
        RequiredOptionBuilder(names, help, converter, hasDefault = true, default = value)

    /** Allows the option to repeat, collecting every occurrence into a `List<T>`. */
    public fun multiple(): OptionListBuilder<T> = OptionListBuilder(names, help, converter)

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, T?> {
        val key = names.first()
        thisRef.register(
            OptionSpec(names = names, converter = converter, help = help, required = false, default = null, multiple = false),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().valueOrNull(key) }
    }
}

/**
 * An option that always resolves to a value, reached via [OptionBuilder.required] or
 * [OptionBuilder.default].
 *
 * @param T the option's value type.
 */
public class RequiredOptionBuilder<T : Any> internal constructor(
    private val names: List<String>,
    private val help: String,
    private val converter: Converter<T>,
    private val hasDefault: Boolean = false,
    private val default: T? = null,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, T>> {

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, T> {
        val key = names.first()
        thisRef.register(
            OptionSpec(
                names = names,
                converter = converter,
                help = help,
                required = !hasDefault,
                default = default,
                multiple = false,
            ),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().value(key) }
    }
}

/**
 * A repeatable option, reached via [OptionBuilder.multiple]. Resolves to an empty list when the
 * option never appears, unless [required] is set.
 *
 * @param T the element type.
 */
public class OptionListBuilder<T : Any> internal constructor(
    private val names: List<String>,
    private val help: String,
    private val converter: Converter<T>,
    private val required: Boolean = false,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, List<T>>> {

    /** Requires at least one occurrence of the option. */
    public fun required(): OptionListBuilder<T> = OptionListBuilder(names, help, converter, true)

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, List<T>> {
        val key = names.first()
        thisRef.register(
            OptionSpec(names = names, converter = converter, help = help, required = required, default = null, multiple = true),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().list(key) }
    }
}
