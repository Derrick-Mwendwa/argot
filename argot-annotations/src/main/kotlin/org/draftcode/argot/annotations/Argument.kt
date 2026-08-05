package org.draftcode.argot.annotations

/**
 * Declares a positional argument on a [Command] constructor parameter, named after the parameter.
 *
 * The parameter's type determines the converter. A `List<T>` becomes a trailing positional that
 * captures the rest (at most one, declared last). As with [Option], a non-nullable type is required
 * and a nullable type is optional.
 *
 * @param help help text for `--help`.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class Argument(
    val help: String = "",
)
