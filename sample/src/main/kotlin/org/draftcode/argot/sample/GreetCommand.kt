package org.draftcode.argot.sample

import org.draftcode.argot.Arguments
import org.draftcode.argot.parsed

/** The `greet` command, demonstrating the delegate style. */
class GreetArgs : Arguments(
    programName = "greet",
    description = "Print a greeting.",
    version = "0.1.0",
) {
    val name: String by option("--name", "-n", help = "Who to greet").default("world")
    val count: Int by option("--count", "-c", help = "How many times to greet").int().default(1)
    val shout: Boolean by flag("--shout", "-s", help = "Uppercase the greeting")
    val extras: List<String> by argument(help = "Additional names to greet").multiple()
}

fun greetingLines(args: GreetArgs): List<String> {
    val names = listOf(args.name) + args.extras
    return buildList {
        repeat(args.count) {
            names.forEach { who ->
                val line = "Hello, $who!"
                add(if (args.shout) line.uppercase() else line)
            }
        }
    }
}

fun runGreet(argv: Array<String>) {
    val args = GreetArgs().parsed(argv)
    greetingLines(args).forEach(::println)
}
