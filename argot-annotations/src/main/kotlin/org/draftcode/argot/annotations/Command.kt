package org.draftcode.argot.annotations

/**
 * Marks a class whose primary-constructor parameters declare a command-line interface.
 *
 * The Argot KSP processor reads the primary constructor — each parameter annotated with [Option],
 * [Flag], or [Argument] — and generates a sibling `parse<ClassName>(argv): ClassName` function. The
 * class itself is never modified.
 *
 * ```
 * @Command(name = "serve", description = "Run the server")
 * data class ServeArgs(
 *     @Option(names = ["--host"], help = "Bind host", default = "0.0.0.0") val host: String,
 *     @Option(names = ["--port", "-p"], help = "Port") val port: Int,
 *     @Flag(names = ["--verbose", "-v"], help = "Verbose") val verbose: Boolean = false,
 *     @Argument(help = "Files to serve") val files: List<String>,
 * )
 * // Generated: fun parseServeArgs(argv: Array<String>): ServeArgs
 * ```
 *
 * @param name the program name shown in usage; defaults to the class's simple name when blank.
 * @param description a one-line description shown at the top of `--help`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Command(
    val name: String = "",
    val description: String = "",
)
