package org.draftcode.argot.annotations

/**
 * Declares a boolean flag on a [Command] constructor parameter.
 *
 * The parameter must be a non-nullable `Boolean`. Presence of the flag implies `true`; absence
 * resolves to `false`. A Kotlin default of `= false` on the parameter is fine and consistent with
 * this flag semantics (it is implied by flag-ness, not read by the processor).
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
