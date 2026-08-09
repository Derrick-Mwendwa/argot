package org.draftcode.argot

/**
 * Base type for all user-input parsing failures. The [cli] wrapper prints [usage] and the
 * [message] to stderr and exits `2`.
 */
public sealed class ArgotParseException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** The command's one-line usage string, attached by the engine before the exception escapes. */
    public var usage: String? = null
        internal set

    /** An unrecognized option or flag name was supplied. */
    public class UnknownOption(public val name: String) :
        ArgotParseException("unknown option: $name")

    /** A required option was not supplied. */
    public class MissingRequiredOption(public val name: String) :
        ArgotParseException("missing required option: $name")

    /** A required positional argument was not supplied. */
    public class MissingRequiredArgument(public val name: String) :
        ArgotParseException("missing required argument: <$name>")

    /** An option that expects a value was supplied without one. */
    public class MissingValue(public val name: String) :
        ArgotParseException("option $name requires a value")

    /**
     * A converter rejected a value.
     *
     * @property detail the converter's own explanation, when it threw an [ArgotConversionException]
     *   carrying one. It replaces the generic wording, because a converter knows what it wanted and
     *   the engine only knows the type's name.
     */
    public class InvalidValue(
        public val name: String,
        public val raw: String,
        public val expectedType: String,
        public val detail: String? = null,
        cause: Throwable? = null,
    ) : ArgotParseException(invalidValueMessage(name, raw, expectedType, detail), cause)

    /** More positional arguments were supplied than the command declares. */
    public class TooManyArguments(public val extra: List<String>) :
        ArgotParseException("unexpected extra argument(s): ${extra.joinToString(" ")}")

    /** A single-valued option was supplied more than once. */
    public class DuplicateValue(public val name: String) :
        ArgotParseException("option $name was supplied more than once")
}

/**
 * A converter's own message wins when it supplied one: it can say "expected 30s, 5m, or 2h" where
 * the engine could only say "expected Duration". Converters conventionally quote the offending value
 * themselves, so the raw is not repeated alongside it.
 */
private fun invalidValueMessage(
    name: String,
    raw: String,
    expectedType: String,
    detail: String?,
): String =
    if (detail.isNullOrBlank()) {
        "invalid value '$raw' for $name (expected $expectedType)"
    } else {
        "invalid value for $name: $detail"
    }
