package org.draftcode.argot

/**
 * Thrown by [ArgotEngine.parse] when `--help` or `-h` is encountered.
 *
 * This is control flow, not an error: it deliberately does **not** extend [ArgotParseException].
 * The engine never prints or exits on its own — it surfaces the fully rendered help text here and
 * lets the caller decide. The [cli] wrapper prints [rendered] to stdout and exits `0`.
 */
public class HelpRequested internal constructor(
    /** The full, ready-to-print help text. */
    public val rendered: String,
) : RuntimeException()

/**
 * Thrown by [ArgotEngine.parse] when `--version` is encountered and a version is configured on the
 * [CommandSpec].
 *
 * Like [HelpRequested], this is control flow rather than an error. The [cli] wrapper prints
 * [rendered] to stdout and exits `0`.
 */
public class VersionRequested internal constructor(
    /** The ready-to-print version line, for example `"serve 1.2.0"`. */
    public val rendered: String,
) : RuntimeException()
