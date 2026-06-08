package org.draftcode.argot

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A fluent builder for a single positional [argument][Arguments.argument], required by default.
 * Also a [PropertyDelegateProvider]. The argument's name is taken from the delegated property.
 *
 * The terminal call determines the delegated type and so the property's nullability:
 * - **bare** (this builder): required; resolves to a non-null value;
 * - [optional]: resolves to `null` when absent, so it backs a nullable property;
 * - [multiple]: a trailing list that captures the rest (see [ArgumentListBuilder]).
 *
 * Refine the value type (`.int()`, `.enum<E>()`, `.convert(...)`) **before** choosing cardinality.
 *
 * @param T the argument's value type.
 */
public class ArgumentBuilder<T : Any> internal constructor(
    private val help: String,
    private val converter: Converter<T>,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, T>> {

    /** Refines the value type using a custom [converter]. */
    public fun <R : Any> convert(converter: Converter<R>): ArgumentBuilder<R> =
        ArgumentBuilder(help, converter)

    /** Refines the value type to [Int]. */
    public fun int(): ArgumentBuilder<Int> = convert(IntConverter)

    /** Refines the value type to [Long]. */
    public fun long(): ArgumentBuilder<Long> = convert(LongConverter)

    /** Refines the value type to [Double]. */
    public fun double(): ArgumentBuilder<Double> = convert(DoubleConverter)

    /** Refines the value type to the enum [E], matching constant names case-insensitively. */
    public inline fun <reified E : Enum<E>> enum(): ArgumentBuilder<E> = convert(enumConverter<E>())

    /** Makes the argument optional; it resolves to `null` when absent. */
    public fun optional(): OptionalArgumentBuilder<T> = OptionalArgumentBuilder(help, converter)

    /** Switches to a trailing list argument that captures all remaining positionals. */
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
 * A fluent builder for an optional positional [argument][Arguments.argument], reached via
 * [ArgumentBuilder.optional]. Resolves to `null` when absent.
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
 * A fluent builder for a trailing list [argument][Arguments.argument] that captures the rest of the
 * positionals. Reached via [ArgumentBuilder.multiple]. Resolves to an empty list when none are
 * supplied unless [required] is set.
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
