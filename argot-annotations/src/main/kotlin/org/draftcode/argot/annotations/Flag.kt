package org.draftcode.argot.annotations

/**
 * Declares a boolean flag on a [Command] constructor parameter.
 *
 * The parameter must be a non-nullable `Boolean`: presence of the flag implies `true`, absence
 * `false`. A Kotlin `= false` default is fine.
 *
 * @param names the flag's names, primary first (for example `["--verbose", "-v"]`).
 * @param help help text for `--help`.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
public annotation class Flag(
    val names: Array<String> = [],
    val help: String = "",
)
