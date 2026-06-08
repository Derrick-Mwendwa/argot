package org.draftcode.argot

/**
 * Base class for the **delegate style** of declaring a command-line interface.
 *
 * Subclasses declare each parameter with a `by` delegate built from [option], [flag], or
 * [argument]. Each delegate registers its [ParamSpec] into this instance as the subclass
 * initializes, so by the time the constructor returns, the full specification is known. Parsing,
 * validation, and help all flow through [ArgotEngine]; this class adds no parsing logic of its own.
 *
 * Reading a delegated property before parsing fails fast (see [parse]/[parsed]).
 *
 * Example:
 * ```
 * class ServerArgs : Arguments(programName = "serve", description = "Run the server") {
 *     val host: String        by option("--host", help = "Bind host").default("0.0.0.0")
 *     val port: Int           by option("--port", "-p", help = "Port").int().required()
 *     val verbose: Boolean    by flag("--verbose", "-v", help = "Verbose logging")
 *     val files: List<String> by argument(help = "Files to serve").multiple()
 * }
 *
 * fun main(argv: Array<String>) {
 *     val args = ServerArgs().parsed(argv)   // validates; prints help / exits on --help or error
 *     println("serving ${args.files} on ${args.host}:${args.port}")
 * }
 * ```
 *
 * The program name is taken as a constructor parameter rather than derived from the class, because
 * deriving it would require runtime class introspection — which Argot deliberately avoids.
 *
 * @param programName the program name shown in usage/help.
 * @param description a one-line description shown at the top of `--help`.
 * @param version an optional version string; when set, `--version` is recognized.
 */
public abstract class Arguments(
    public val programName: String = "program",
    public val description: String = "",
    public val version: String? = null,
) {
    private val specs = mutableListOf<ParamSpec>()
    private var parsedValues: ParsedValues? = null

    /** Registers a delegate's spec. Called from the builders' `provideDelegate`. */
    internal fun register(spec: ParamSpec) {
        specs.add(spec)
    }

    /** Returns the parsed values, or fails fast if parsing has not happened yet. */
    internal fun resolved(): ParsedValues =
        parsedValues
            ?: error(
                "Arguments have not been parsed yet. " +
                    "Call parse(argv) or parsed(argv) before reading a delegated property.",
            )

    /** Builds the [CommandSpec] from the registered specs and runs the engine. */
    internal fun populate(argv: Array<String>) {
        val spec = CommandSpec(programName, description, specs.toList(), version)
        parsedValues = ArgotEngine.parse(spec, argv)
    }

    /**
     * Begins a value-bearing option. Defaults to a `String` value; refine the type with `.int()`,
     * `.long()`, `.double()`, `.enum<E>()`, or `.convert(...)`, and the cardinality/optionality
     * with `.default(...)`, `.required()`, or `.multiple()`.
     *
     * @param names the option's names, primary first (for example `"--port", "-p"`).
     * @param help help text for `--help`.
     */
    protected fun option(vararg names: String, help: String = ""): OptionBuilder<String> =
        OptionBuilder(names.toList(), help, StringConverter)

    /**
     * Begins a boolean flag (presence implies `true`). Refine with `.default(...)` or
     * `.negatedBy(...)`.
     *
     * @param names the flag's names, primary first (for example `"--verbose", "-v"`).
     * @param help help text for `--help`.
     */
    protected fun flag(vararg names: String, help: String = ""): FlagBuilder =
        FlagBuilder(names.toList(), help)

    /**
     * Begins a positional argument. The argument's name comes from the delegated property. Defaults
     * to a required `String`; refine with `.int()`/`.enum<E>()`/`.convert(...)`, `.optional()`, or
     * `.multiple()` (a trailing list that captures the rest).
     *
     * @param help help text for `--help`.
     */
    protected fun argument(help: String = ""): ArgumentBuilder<String> =
        ArgumentBuilder(help, StringConverter)
}

/**
 * Parses [argv] into this instance and returns it, **without** any console side effects.
 *
 * @throws HelpRequested if `--help`/`-h` is present.
 * @throws VersionRequested if `--version` is present and a version is configured.
 * @throws ArgotParseException for any user-input error.
 */
public fun <T : Arguments> T.parse(argv: Array<String>): T {
    populate(argv)
    return this
}

/**
 * Parses [argv] into this instance and returns it, adapting Argot's outcomes to a CLI process
 * lifecycle via [cli]: prints help/version to stdout and exits `0`, or prints usage + error to
 * stderr and exits `2`.
 */
public fun <T : Arguments> T.parsed(argv: Array<String>): T = cli { parse(argv) }
