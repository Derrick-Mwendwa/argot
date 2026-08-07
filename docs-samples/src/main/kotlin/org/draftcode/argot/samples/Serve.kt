package org.draftcode.argot.samples

// #region declare
import org.draftcode.argot.annotations.Argument
import org.draftcode.argot.annotations.Command
import org.draftcode.argot.annotations.Flag
import org.draftcode.argot.annotations.Option

@Command(name = "serve", description = "Run the server.")
data class ServeArgs(
    @Option(names = ["--host"], help = "Bind host", default = "0.0.0.0") val host: String,
    @Option(names = ["--port", "-p"], help = "Port") val port: Int,
    @Flag(names = ["--verbose", "-v"], help = "Verbose logging") val verbose: Boolean = false,
    @Argument(help = "Files to serve") val files: List<String>,
)
// #endregion declare

// #region use
fun describe(args: ServeArgs): String =
    "serving ${args.files.size} file(s) on ${args.host}:${args.port}"
// #endregion use
