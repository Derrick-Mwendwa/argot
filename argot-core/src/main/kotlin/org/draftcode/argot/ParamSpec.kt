package org.draftcode.argot

/** A declared command-line parameter: an [OptionSpec], [FlagSpec], or [ArgumentSpec]. */
public sealed class ParamSpec {
    /** Help text shown for this parameter in `--help`. */
    public abstract val help: String
}

/**
 * A named parameter that takes a value, for example `--count 3`, `--count=3`, or `-c 3`.
 *
 * @param names the option's names, primary first, for example `["--count", "-c"]`.
 * @param converter converts each raw value to the typed result.
 * @param help help text for `--help`.
 * @param required whether at least one occurrence must be supplied.
 * @param default the already-converted value used when a single-valued option is absent.
 * @param multiple when true repeats accumulate into a list; when false a second occurrence is an
 *   [ArgotParseException.DuplicateValue].
 */
public class OptionSpec(
    public val names: List<String>,
    public val converter: Converter<*>,
    override val help: String = "",
    public val required: Boolean = false,
    public val default: Any? = null,
    public val multiple: Boolean = false,
) : ParamSpec() {
    init {
        require(names.isNotEmpty()) { "OptionSpec requires at least one name" }
    }

    /** The first of [names], used as the lookup key in [ParsedValues]. */
    public val primaryName: String get() = names.first()
}

/**
 * A named boolean parameter whose presence means `true`.
 *
 * @param names the flag's names, primary first, for example `["--verbose", "-v"]`.
 * @param help help text for `--help`.
 * @param negationNames names that set the flag to `false`, for example `["--no-verbose"]`.
 * @param default the value used when neither a name nor a negation name is present.
 */
public class FlagSpec(
    public val names: List<String>,
    override val help: String = "",
    public val negationNames: List<String> = emptyList(),
    public val default: Boolean = false,
) : ParamSpec() {
    init {
        require(names.isNotEmpty()) { "FlagSpec requires at least one name" }
    }

    /** The first of [names], used as the lookup key in [ParsedValues]. */
    public val primaryName: String get() = names.first()
}

/**
 * A positional parameter, consumed left to right from the non-option arguments.
 *
 * @param name the parameter name, shown in help and used as the lookup key in [ParsedValues].
 * @param converter converts each raw value to the typed result.
 * @param help help text for `--help`.
 * @param required whether at least one value must be supplied.
 * @param multiple when true, captures all remaining positionals. At most one parameter per command
 *   may set this, and it must be declared last.
 */
public class ArgumentSpec(
    public val name: String,
    public val converter: Converter<*>,
    override val help: String = "",
    public val required: Boolean = true,
    public val multiple: Boolean = false,
) : ParamSpec()
