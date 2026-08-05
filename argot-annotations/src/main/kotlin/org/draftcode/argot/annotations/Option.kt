package org.draftcode.argot.annotations

/**
 * Declares a value-bearing named option on a [Command] constructor parameter.
 *
 * The parameter's type determines the converter (`String`, `Int`, `Long`, `Double`, `Boolean`, an
 * `enum`, or `List<T>` of those for a repeatable option). Optionality:
 * - a non-nullable type with no [default] is **required**;
 * - a nullable type is optional, resolving to `null` when absent;
 * - a [default] is optional, parsed via the parameter's converter when the option is absent.
 *
 * @param names the option's names, primary first (for example `["--port", "-p"]`).
 * @param help help text for `--help`.
 * @param default the default value as a string; leave unset to mean "no default".
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Option(
    val names: Array<String> = [],
    val help: String = "",
    val default: String = NO_DEFAULT,
)
