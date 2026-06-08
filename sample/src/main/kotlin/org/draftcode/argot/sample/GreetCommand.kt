package org.draftcode.argot.sample

import org.draftcode.argot.Arguments
import org.draftcode.argot.parsed

/**
 * The `greet` command, implemented with Argot's **delegate style** (zero codegen).
 *
 * Demonstrates defaults, an int option, a flag, and a trailing positional list — all declared with
 * `by` delegates that register their specs into [Arguments] as the instance initializes.
 */
public class GreetArgs : Arguments(
    programName = "greet",
    description = "Print a greeting.",
    version = "0.1.0",
) {
    /** Who to greet. */
    public val name: String by option("--name", "-n", help = "Who to greet").default("world")

    /** How many times to repeat the greeting. */
    public val count: Int by option("--count", "-c", help = "How many times to greet").int().default(1)

    /** Uppercase the greeting. */
    public val shout: Boolean by flag("--shout", "-s", help = "Uppercase the greeting")

    /** Additional names to also greet. */
    public val extras: List<String> by argument(help = "Additional names to greet").multiple()
}

/**
 * Pure formatting logic for the `greet` command, separated from I/O so it is directly testable.
 */
public fun greetingLines(args: GreetArgs): List<String> {
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

/** Runs the `greet` command: parses [argv] (handling `--help`/errors via `cli`) and prints. */
public fun runGreet(argv: Array<String>) {
    val args = GreetArgs().parsed(argv)
    greetingLines(args).forEach(::println)
}
