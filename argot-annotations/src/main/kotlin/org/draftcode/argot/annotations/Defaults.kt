package org.draftcode.argot.annotations

/**
 * Sentinel value for [Option.default] meaning "no default was supplied".
 *
 * Annotation arguments cannot be `null`, so a sentinel string distinguishes "absent" from an
 * explicit empty-string default (`default = ""`). The value is the null character (`U+0000`),
 * which cannot occur in a real command-line default. The KSP processor compares against this
 * constant; consumers should not use it directly.
 */
public const val NO_DEFAULT: String = "\u0000"
