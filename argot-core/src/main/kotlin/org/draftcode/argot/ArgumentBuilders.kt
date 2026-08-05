package org.draftcode.argot

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Builds a positional argument, named after the property it is assigned to. The call you end with
 * decides the property's type:
 * - end here and the argument is required and non-null;
 * - [optional] makes it nullable, resolving to `null` when absent;
 * - [multiple] captures all remaining positionals into a `List<T>`.
 *
 * Refine the value type (`.int()`, `.enum<E>()`, `.convert(...)`) before choosing cardinality.
 *
 * @param T the argument's value type.
 */
public class ArgumentBuilder<T : Any> internal constructor(
    private val help: String,
    private val converter: Converter<T>,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, T>> {

    public fun <R : Any> convert(converter: Converter<R>): ArgumentBuilder<R> =
        ArgumentBuilder(help, converter)

    public fun int(): ArgumentBuilder<Int> = convert(IntConverter)

    public fun long(): ArgumentBuilder<Long> = convert(LongConverter)

    public fun double(): ArgumentBuilder<Double> = convert(DoubleConverter)

    /** Parses the value as the enum [E], matching constant names case-insensitively. */
    public inline fun <reified E : Enum<E>> enum(): ArgumentBuilder<E> = convert(enumConverter<E>())

    /** Makes the argument optional; it resolves to `null` when absent. */
    public fun optional(): OptionalArgumentBuilder<T> = OptionalArgumentBuilder(help, converter)

    /** Captures all remaining positionals into a `List<T>`. Must be the last argument declared. */
    public fun multiple(): ArgumentListBuilder<T> = ArgumentListBuilder(help, converter)

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, T> {
        val name = property.name
        thisRef.register(
            ArgumentSpec(name = name, converter = converter, help = help, required = true, multiple = false),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().value(name) }
    }
}

/**
 * An optional positional argument, reached via [ArgumentBuilder.optional]. Resolves to `null` when
 * absent.
 *
 * @param T the argument's value type.
 */
public class OptionalArgumentBuilder<T : Any> internal constructor(
    private val help: String,
    private val converter: Converter<T>,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, T?>> {

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, T?> {
        val name = property.name
        thisRef.register(
            ArgumentSpec(name = name, converter = converter, help = help, required = false, multiple = false),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().valueOrNull(name) }
    }
}

/**
 * A trailing list argument, reached via [ArgumentBuilder.multiple]. Resolves to an empty list when
 * no positionals remain, unless [required] is set.
 *
 * @param T the element type.
 */
public class ArgumentListBuilder<T : Any> internal constructor(
    private val help: String,
    private val converter: Converter<T>,
    private val required: Boolean = false,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, List<T>>> {

    /** Requires at least one value. */
    public fun required(): ArgumentListBuilder<T> = ArgumentListBuilder(help, converter, true)

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, List<T>> {
        val name = property.name
        thisRef.register(
            ArgumentSpec(name = name, converter = converter, help = help, required = required, multiple = true),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().list(name) }
    }
}
