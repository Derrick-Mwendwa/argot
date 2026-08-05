package org.draftcode.argot.sample

import org.draftcode.argot.annotations.Argument
import org.draftcode.argot.annotations.Command
import org.draftcode.argot.annotations.Flag
import org.draftcode.argot.annotations.Option
import org.draftcode.argot.cli

/** The `serve` command, demonstrating the annotation style (a `parseServeArgs` function is generated). */
@Command(name = "serve", description = "Run the server.")
data class ServeArgs(
    @Option(names = ["--host"], help = "Bind host", default = "0.0.0.0") val host: String,
    @Option(names = ["--port", "-p"], help = "Port") val port: Int,
    @Flag(names = ["--verbose", "-v"], help = "Verbose") val verbose: Boolean = false,
    @Argument(help = "Files to serve") val files: List<String>,
)

fun serveSummary(args: ServeArgs): String =
    "serving ${args.files} on ${args.host}:${args.port} (verbose=${args.verbose})"

fun runServe(argv: Array<String>) {
    val args = cli { parseServeArgs(argv) }
    println(serveSummary(args))
}
