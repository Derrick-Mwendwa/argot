package org.draftcode.argot.annotations

/**
 * Marks a class whose primary-constructor parameters declare a command-line interface.
 *
 * The Argot KSP processor reads the annotated class's primary constructor, maps each parameter
 * (via its [Option], [Flag], or [Argument] annotation) to a parameter spec, and generates a
 * sibling top-level `parse<ClassName>(argv): ClassName` function. The class itself is never
 * modified.
 *
 * These annotations have `SOURCE` retention: they exist only at compile time and leave no runtime
 * footprint, consistent with Argot's "no runtime reflection" guarantee.
 *
 * Example:
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
 * @param name the program name shown in usage/help; defaults to the class's simple name when blank.
 * @param description a one-line description shown at the top of `--help`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Command(
    val name: String = "",
    val description: String = "",
)
