package org.draftcode.argot.annotations

/**
 * Declares a value-bearing named option on a [Command] constructor parameter.
 *
 * The parameter's Kotlin type determines the converter (`String`, `Int`, `Long`, `Double`,
 * `Boolean`, any `enum`, or `List<T>` of those for a repeatable option). Optionality follows
 * Argot's v1 rule, which uses only what KSP can see:
 * - a non-nullable type with no [default] ⇒ **required**;
 * - a nullable type ⇒ optional, resolving to `null` when absent;
 * - a [default] string ⇒ optional, parsed via the parameter's converter when absent.
 *
 * @param names the option's names, primary first (for example `["--port", "-p"]`).
 * @param help help text for `--help`.
 * @param default the default value as a string, parsed by the parameter's converter when the
 *   option is absent. Leave unset (the [NO_DEFAULT] sentinel) to mean "no default".
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Option(
    val names: Array<String> = [],
    val help: String = "",
    val default: String = NO_DEFAULT,
)
