package org.draftcode.argot

/**
 * Base class for declaring a command line with `by` delegates.
 *
 * Declare each parameter with [option], [flag], or [argument]. Reading a delegated property before
 * the arguments have been parsed throws.
 *
 * ```
 * class ServerArgs : Arguments(programName = "serve", description = "Run the server") {
 *     val host: String        by option("--host", help = "Bind host").default("0.0.0.0")
 *     val port: Int           by option("--port", "-p", help = "Port").int().required()
 *     val verbose: Boolean    by flag("--verbose", "-v", help = "Verbose logging")
 *     val files: List<String> by argument(help = "Files to serve").multiple()
 * }
 *
 * fun main(argv: Array<String>) {
 *     val args = ServerArgs().parsed(argv)
 *     println("serving ${args.files} on ${args.host}:${args.port}")
 * }
 * ```
 *
 * @param programName the program name shown in usage and help.
 * @param description a one-line description shown at the top of `--help`.
 * @param version when set, `--version` is recognized.
 */
public abstract class Arguments(
    public val programName: String = "program",
    public val description: String = "",
    public val version: String? = null,
) {
    private val specs = mutableListOf<ParamSpec>()
    private var parsedValues: ParsedValues? = null

    internal fun register(spec: ParamSpec) {
        specs.add(spec)
    }

    internal fun resolved(): ParsedValues =
        parsedValues
            ?: error(
                "Arguments have not been parsed yet. " +
                    "Call parse(argv) or parsed(argv) before reading a delegated property.",
            )

    internal fun populate(argv: Array<String>) {
        val spec = CommandSpec(programName, description, specs.toList(), version)
        parsedValues = ArgotEngine.parse(spec, argv)
    }

    /**
     * Declares an option that takes a value. The value is a `String` unless you refine it with
     * `.int()`, `.long()`, `.double()`, `.boolean()`, `.enum<E>()`, or `.convert(...)`.
     *
     * @param names the option's names, primary first, for example `"--port", "-p"`.
     * @param help help text for `--help`.
     */
    protected fun option(vararg names: String, help: String = ""): OptionBuilder<String> =
        OptionBuilder(names.toList(), help, StringConverter)

    /**
     * Declares a boolean flag whose presence means `true`.
     *
     * @param names the flag's names, primary first, for example `"--verbose", "-v"`.
     * @param help help text for `--help`.
     */
    protected fun flag(vararg names: String, help: String = ""): FlagBuilder =
        FlagBuilder(names.toList(), help)

    /**
     * Declares a positional argument, named after the property it is assigned to. Required and of
     * type `String` unless refined.
     *
     * @param help help text for `--help`.
     */
    protected fun argument(help: String = ""): ArgumentBuilder<String> =
        ArgumentBuilder(help, StringConverter)
}

/**
 * Parses [argv] into this instance and returns it, without printing or exiting.
 *
 * @throws HelpRequested if `--help` or `-h` is present.
 * @throws VersionRequested if `--version` is present and a version is configured.
 * @throws ArgotParseException if the input is invalid.
 */
public fun <T : Arguments> T.parse(argv: Array<String>): T {
    populate(argv)
    return this
}

/** Parses [argv] into this instance via [cli], printing help or errors and exiting as appropriate. */
public fun <T : Arguments> T.parsed(argv: Array<String>): T = cli { parse(argv) }
