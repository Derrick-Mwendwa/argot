package org.draftcode.argot.samples

// #region declare
import org.draftcode.argot.annotations.Command
import org.draftcode.argot.annotations.Flag
import org.draftcode.argot.annotations.Option

@Command(name = "greet", description = "Print a friendly greeting.")
data class GreetCommand(
    @Option(names = ["--name", "-n"], help = "Who to greet", default = "world")
    val name: String,
    @Option(names = ["--count", "-c"], help = "How many times", default = "1")
    val count: Int,
    @Flag(names = ["--loud", "-l"], help = "Shout the greeting")
    val loud: Boolean = false,
)
// #endregion declare

// #region use
fun greetings(args: GreetCommand): List<String> {
    val line = "Hello, ${args.name}!"
    return List(args.count) { if (args.loud) line.uppercase() else line }
}
// #endregion use
