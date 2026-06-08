package org.draftcode.argot.annotations

/**
 * Declares a positional argument on a [Command] constructor parameter.
 *
 * The parameter's Kotlin type determines the converter. A `List<T>` parameter becomes a trailing
 * positional that captures all remaining arguments; at most one such parameter is allowed and it
 * must be declared last. Optionality follows the same rule as [Option]: a non-nullable type is
 * required, a nullable type is optional and resolves to `null` when absent.
 *
 * The argument's name (used in help and usage) is taken from the parameter name.
 *
 * @param help help text for `--help`.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Argument(
    val help: String = "",
)
