package org.draftcode.argot

/**
 * A single declared command-line parameter.
 *
 * Both consumer styles (delegates and annotations) build a list of [ParamSpec] and hand it to the
 * engine, so parsing behavior is identical regardless of how the spec was authored. This is a
 * sealed hierarchy: every parameter is exactly one of [OptionSpec], [FlagSpec], or [ArgumentSpec].
 */
public sealed class ParamSpec {
    /** Human-readable help shown for this parameter in `--help` output. */
    public abstract val help: String
}

/**
 * A value-bearing named parameter, for example `--count 3`, `--count=3`, or `-c 3`.
 *
 * @param names the option's names, primary first (for example `["--count", "-c"]`). At least one
 *   is required, and the first is the canonical lookup key in [ParsedValues].
 * @param help help text for `--help`.
 * @param converter converts each raw value to the typed result.
 * @param required whether at least one occurrence must be supplied. For a [multiple] option,
 *   `required` means at least one occurrence is mandatory; otherwise an empty list is produced.
 * @param default the already-converted value used when the option is absent (single-valued only).
 * @param multiple when true, repeats accumulate into a list; when false, a second occurrence is a
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

    /** The canonical name (first of [names]), used as the lookup key in [ParsedValues]. */
    public val primaryName: String get() = names.first()
}

/**
 * A boolean named parameter whose presence implies `true`.
 *
 * @param names the flag's names, primary first (for example `["--verbose", "-v"]`).
 * @param help help text for `--help`.
 * @param negationNames optional names that explicitly set the flag to `false` (for example
 *   `["--no-verbose"]`). Optional in v1.
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

    /** The canonical name (first of [names]), used as the lookup key in [ParsedValues]. */
    public val primaryName: String get() = names.first()
}

/**
 * A positional parameter, consumed left to right from the non-option arguments.
 *
 * @param name the parameter name, used for help and as the lookup key in [ParsedValues].
 * @param converter converts each raw value to the typed result.
 * @param help help text for `--help`.
 * @param required whether at least one value must be supplied. For a [multiple] positional,
 *   `required` means "at least one".
 * @param multiple when true, captures all remaining positionals into a list. At most one
 *   `multiple` positional is allowed per command, and it must be declared last.
 */
public class ArgumentSpec(
    public val name: String,
    public val converter: Converter<*>,
    override val help: String = "",
    public val required: Boolean = true,
    public val multiple: Boolean = false,
) : ParamSpec()
