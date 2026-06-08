package org.draftcode.argot

/**
 * The complete specification of a command: its identity, description, optional version, and the
 * ordered list of parameters.
 *
 * A [CommandSpec] owns validation of its own shape (see [validate]) and the rendering of `--help`
 * and usage output (see [renderHelp] and [renderUsage]). Because both consumer styles produce a
 * [CommandSpec] and hand it to [ArgotEngine], their help output and parsing behavior are identical.
 *
 * @param programName the program name shown in usage (for example `serve`).
 * @param description a one-line description shown at the top of `--help` (may be blank).
 * @param params the declared parameters, in declaration order.
 * @param version an optional version string; when non-null, `--version` is recognized.
 */
public class CommandSpec(
    public val programName: String,
    public val description: String = "",
    public val params: List<ParamSpec>,
    public val version: String? = null,
) {
    /** The named value-bearing options, in declaration order. */
    internal val options: List<OptionSpec> = params.filterIsInstance<OptionSpec>()

    /** The boolean flags, in declaration order. */
    internal val flags: List<FlagSpec> = params.filterIsInstance<FlagSpec>()

    /** The positional arguments, in declaration order. */
    internal val arguments: List<ArgumentSpec> = params.filterIsInstance<ArgumentSpec>()

    /**
     * Validates the *shape* of this specification and throws [IllegalArgumentException] if it is
     * malformed. This guards against programming errors (duplicate names, an ill-placed `multiple`
     * positional, blank names, and so on); user-input errors are reported separately as
     * [ArgotParseException]s during parsing.
     *
     * The delegate DSL and the generated annotation code always produce valid specifications, so in
     * practice this is a defense-in-depth check. The KSP processor performs equivalent checks at
     * compile time for earlier, friendlier feedback.
     */
    public fun validate() {
        val issues = collectIssues()
        require(issues.isEmpty()) {
            buildString {
                append("invalid command specification for '$programName':")
                issues.forEach { append("\n  - ").append(it) }
            }
        }
    }

    private fun collectIssues(): List<String> {
        val issues = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        fun checkNamed(name: String, kind: String) {
            when {
                name.isBlank() -> issues += "$kind has a blank name"
                !name.startsWith("-") -> issues += "$kind name '$name' must start with '-'"
            }
            if (name.isNotBlank() && !seen.add(name)) {
                issues += "duplicate name '$name'"
            }
        }

        options.forEach { opt -> opt.names.forEach { checkNamed(it, "option") } }
        flags.forEach { flag ->
            flag.names.forEach { checkNamed(it, "flag") }
            flag.negationNames.forEach { checkNamed(it, "flag negation") }
        }
        arguments.forEach { arg ->
            if (arg.name.isBlank()) issues += "argument has a blank name"
        }

        val multipleCount = arguments.count { it.multiple }
        if (multipleCount > 1) {
            issues += "at most one 'multiple' positional is allowed (found $multipleCount)"
        }
        var sawOptionalOrMultiple = false
        arguments.forEachIndexed { index, arg ->
            if (arg.multiple && index != arguments.lastIndex) {
                issues += "the 'multiple' positional '${arg.name}' must be declared last"
            }
            if (sawOptionalOrMultiple && arg.required && !arg.multiple) {
                issues += "required positional '${arg.name}' cannot follow an optional or 'multiple' positional"
            }
            if (!arg.required || arg.multiple) sawOptionalOrMultiple = true
        }
        return issues
    }

    /** Renders the single-line usage string, for example `Usage: serve [options] <files>...`. */
    public fun renderUsage(): String = buildString {
        append("Usage: ").append(programName)
        if (options.isNotEmpty() || flags.isNotEmpty()) append(" [options]")
        arguments.forEach { arg ->
            append(' ')
            val inner = if (arg.multiple) "<${arg.name}>..." else "<${arg.name}>"
            append(if (arg.required) inner else "[$inner]")
        }
    }

    /**
     * Renders the full, aligned `--help` text: an optional description, the usage line, an
     * `Options:` section, and an `Arguments:` section. Output is deterministic. The `Options:`
     * section lists options and flags in declaration order, followed by the synthetic `--help`
     * and, when configured, `--version` entries; the `Arguments:` section lists positionals in
     * declaration order.
     */
    public fun renderHelp(): String {
        val optionRows = buildList {
            // Preserve the order in which options and flags were declared, even when interleaved.
            params.forEach { param ->
                when (param) {
                    is OptionSpec -> add(optionInvocation(param) to optionHelp(param))
                    is FlagSpec -> add(flagInvocation(param) to flagHelp(param))
                    is ArgumentSpec -> Unit // rendered in the Arguments section below
                }
            }
            add("-h, --help" to "Show this help message and exit")
            if (version != null) add("--version" to "Show version information and exit")
        }
        val argumentRows = arguments.map { "<${it.name}>" to it.help }

        val labelWidth = (optionRows + argumentRows).maxOf { it.first.length }

        return buildString {
            if (description.isNotBlank()) {
                append(description).append('\n').append('\n')
            }
            append(renderUsage())
            append('\n').append('\n')
            append("Options:")
            optionRows.forEach { (label, help) -> append('\n').append(formatRow(label, help, labelWidth)) }
            if (argumentRows.isNotEmpty()) {
                append('\n').append('\n').append("Arguments:")
                argumentRows.forEach { (label, help) -> append('\n').append(formatRow(label, help, labelWidth)) }
            }
        }
    }

    private fun optionInvocation(opt: OptionSpec): String =
        opt.names.joinToString(", ") + " <${opt.converter.typeName}>"

    private fun optionHelp(opt: OptionSpec): String = buildString {
        append(opt.help)
        when {
            opt.default != null -> appendSuffix("default: ${opt.default}")
            opt.required -> appendSuffix("required")
        }
    }

    private fun flagInvocation(flag: FlagSpec): String =
        (flag.names + flag.negationNames).joinToString(", ")

    private fun flagHelp(flag: FlagSpec): String = buildString {
        append(flag.help)
        if (flag.default) appendSuffix("default: true")
    }

    private fun StringBuilder.appendSuffix(text: String) {
        if (isNotEmpty()) append(' ')
        append('(').append(text).append(')')
    }

    private fun formatRow(label: String, help: String, labelWidth: Int): String {
        val padded = label.padEnd(labelWidth)
        return if (help.isBlank()) "  $label" else "  $padded  $help"
    }
}
