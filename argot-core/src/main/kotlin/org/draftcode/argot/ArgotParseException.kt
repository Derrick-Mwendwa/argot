package org.draftcode.argot

/**
 * Base type for all user-input parsing failures. The [cli] wrapper prints [usage] and the
 * [message] to stderr and exits `2`.
 */
public sealed class ArgotParseException(message: String) : Exception(message) {
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

    /** A converter rejected a value. */
    public class InvalidValue(
        public val name: String,
        public val raw: String,
        public val expectedType: String,
    ) : ArgotParseException("invalid value '$raw' for $name (expected $expectedType)")

    /** More positional arguments were supplied than the command declares. */
    public class TooManyArguments(public val extra: List<String>) :
        ArgotParseException("unexpected extra argument(s): ${extra.joinToString(" ")}")

    /** A single-valued option was supplied more than once. */
    public class DuplicateValue(public val name: String) :
        ArgotParseException("option $name was supplied more than once")
}
