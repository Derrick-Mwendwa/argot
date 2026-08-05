package org.draftcode.argot

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Builds a boolean flag. A flag name means `true`, a negation name means `false`, and absence means
 * [default].
 */
public class FlagBuilder internal constructor(
    private val names: List<String>,
    private val help: String,
    private val negationNames: List<String> = emptyList(),
    private val default: Boolean = false,
) : PropertyDelegateProvider<Arguments, ReadOnlyProperty<Arguments, Boolean>> {

    /** Sets the value used when neither a flag name nor a negation name is present. */
    public fun default(value: Boolean): FlagBuilder =
        FlagBuilder(names, help, negationNames, value)

    /** Adds names that set the flag to `false`, for example `"--no-verbose"`. */
    public fun negatedBy(vararg negation: String): FlagBuilder =
        FlagBuilder(names, help, negationNames + negation.toList(), default)

    override operator fun provideDelegate(
        thisRef: Arguments,
        property: KProperty<*>,
    ): ReadOnlyProperty<Arguments, Boolean> {
        val key = names.first()
        thisRef.register(FlagSpec(names = names, help = help, negationNames = negationNames, default = default))
        return ReadOnlyProperty { ref, _ -> ref.resolved().flag(key) }
    }
}
