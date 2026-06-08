package org.draftcode.argot

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A fluent builder for a value-bearing [option][Arguments.option] in its initial, optional state.
 * Also a [PropertyDelegateProvider], so a property can delegate to it directly with `by`.
 *
 * The terminal call determines the delegated type and so the property's nullability:
 * - **bare** (this builder): optional; resolves to `null` when absent, so it backs a nullable
 *   property (for example `val retries: Int? by option("--retries").int()`);
 * - [required] or [default]: resolves to a non-null value (see [RequiredOptionBuilder]);
 * - [multiple]: accumulates repeats into a `List<T>` (see [OptionListBuilder]).
 *
 * Refine the value type (`.int()`, `.enum<E>()`, `.convert(...)`) **before** choosing cardinality.
 *
 * @param T the option's value type.
 */
public class OptionBuilder<T : Any> internal constructor(
    private val names: List<String>,
    private val help: String,
    private val converter: Converter<T>,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, T?>> {

    /** Refines the value type using a custom [converter]. */
    public fun <R : Any> convert(converter: Converter<R>): OptionBuilder<R> =
        OptionBuilder(names, help, converter)

    /** Refines the value type to [Int]. */
    public fun int(): OptionBuilder<Int> = convert(IntConverter)

    /** Refines the value type to [Long]. */
    public fun long(): OptionBuilder<Long> = convert(LongConverter)

    /** Refines the value type to [Double]. */
    public fun double(): OptionBuilder<Double> = convert(DoubleConverter)

    /** Refines the value type to [Boolean] (parsed from the option's string value). */
    public fun boolean(): OptionBuilder<Boolean> = convert(BooleanConverter)

    /** Refines the value type to the enum [E], matching constant names case-insensitively. */
    public inline fun <reified E : Enum<E>> enum(): OptionBuilder<E> = convert(enumConverter<E>())

    /** Makes the option required (non-null). */
    public fun required(): RequiredOptionBuilder<T> =
        RequiredOptionBuilder(names, help, converter)

    /** Provides a default [value] used when the option is absent (non-null). */
    public fun default(value: T): RequiredOptionBuilder<T> =
        RequiredOptionBuilder(names, help, converter, hasDefault = true, default = value)

    /** Makes the option repeatable; repeats accumulate into a `List<T>`. */
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
 * A fluent builder for a value-bearing [option][Arguments.option] that resolves to a non-null
 * value, reached via [OptionBuilder.required] or [OptionBuilder.default].
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
                // A default makes the option optional; otherwise it is required.
                required = !hasDefault,
                default = default,
                multiple = false,
            ),
        )
        return ReadOnlyProperty { ref, _ -> ref.resolved().value(key) }
    }
}

/**
 * A fluent builder for a repeatable [option][Arguments.option] that accumulates into a `List<T>`.
 * Reached via [OptionBuilder.multiple]. Resolves to an empty list when the option never appears.
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
