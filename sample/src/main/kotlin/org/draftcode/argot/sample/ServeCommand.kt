package org.draftcode.argot.sample

import org.draftcode.argot.annotations.Argument
import org.draftcode.argot.annotations.Command
import org.draftcode.argot.annotations.Flag
import org.draftcode.argot.annotations.Option
import org.draftcode.argot.cli

/**
 * The `serve` command, implemented with Argot's **annotation style**.
 *
 * The Argot KSP processor reads this class's primary constructor and generates a sibling
 * `parseServeArgs(argv): ServeArgs` function (see the build's `ksp(project(":argot-processor"))`
 * wiring). Optionality follows the v1 rule: `port` is non-null with no default, so it is required;
 * `host` has an annotation `default`, so it is optional; `files` is a trailing positional list.
 */
@Command(name = "serve", description = "Run the server.")
public data class ServeArgs(
    @Option(names = ["--host"], help = "Bind host", default = "0.0.0.0") val host: String,
    @Option(names = ["--port", "-p"], help = "Port") val port: Int,
    @Flag(names = ["--verbose", "-v"], help = "Verbose") val verbose: Boolean = false,
    @Argument(help = "Files to serve") val files: List<String>,
)

/** Pure summary logic for the `serve` command, separated from I/O so it is directly testable. */
public fun serveSummary(args: ServeArgs): String =
    "serving ${args.files} on ${args.host}:${args.port} (verbose=${args.verbose})"

/** Runs the `serve` command using the generated parser, handling `--help`/errors via `cli`. */
public fun runServe(argv: Array<String>) {
    val args = cli { parseServeArgs(argv) }
    println(serveSummary(args))
}
