package org.draftcode.argot

/** Thrown when `--help` or `-h` is present. Not an error: [cli] prints [rendered] and exits `0`. */
public class HelpRequested internal constructor(
    public val rendered: String,
) : RuntimeException()

/**
 * Thrown when `--version` is present and [CommandSpec.version] is set. [cli] prints [rendered] and
 * exits `0`.
 */
public class VersionRequested internal constructor(
    public val rendered: String,
) : RuntimeException()
