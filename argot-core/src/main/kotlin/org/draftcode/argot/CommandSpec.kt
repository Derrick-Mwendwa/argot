package org.draftcode.argot

/**
 * Describes a command: its name, description, optional version, and parameters.
 *
 * @param programName the program name shown in usage.
 * @param description a one-line description shown at the top of `--help`.
 * @param params the declared parameters, in the order they should appear in `--help`.
 * @param version when set, `--version` is recognized and prints this string.
 */
public class CommandSpec(
    public val programName: String,
    public val description: String = "",
    public val params: List<ParamSpec>,
    public val version: String? = null,
) {
    internal val options: List<OptionSpec> = params.filterIsInstance<OptionSpec>()
    internal val flags: List<FlagSpec> = params.filterIsInstance<FlagSpec>()
    internal val arguments: List<ArgumentSpec> = params.filterIsInstance<ArgumentSpec>()

    /**
     * Checks this specification for problems such as duplicate names, blank names, or a misplaced
     * `multiple` positional.
     *
     * @throws IllegalArgumentException if the specification is malformed.
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

    /** Renders the full `--help` text: description, usage line, then aligned options and arguments. */
    public fun renderHelp(): String {
        val optionRows = buildList {
            params.forEach { param ->
                when (param) {
                    is OptionSpec -> add(optionInvocation(param) to optionHelp(param))
                    is FlagSpec -> add(flagInvocation(param) to flagHelp(param))
                    is ArgumentSpec -> Unit
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
