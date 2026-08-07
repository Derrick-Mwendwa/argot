package org.draftcode.argot.samples

// #region declare
import org.draftcode.argot.Arguments

class GreetArgs : Arguments(
    programName = "greet",
    description = "Print a friendly greeting.",
) {
    val name: String by option("--name", "-n", help = "Who to greet").default("world")
    val count: Int by option("--count", "-c", help = "How many times").int().default(1)
    val loud: Boolean by flag("--loud", "-l", help = "Shout the greeting")
}
// #endregion declare

// #region use
fun greetings(args: GreetArgs): List<String> {
    val line = "Hello, ${args.name}!"
    return List(args.count) { if (args.loud) line.uppercase() else line }
}
// #endregion use
