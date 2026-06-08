package org.draftcode.argot.sample

/**
 * Entry point for the Argot sample application.
 *
 * The first argument selects a command, demonstrating both consumer styles in one app:
 * - `greet` — the delegate style ([GreetArgs]);
 * - `serve` — the annotation style (added in Milestone 5).
 *
 * This is plain `when` routing, not Argot subcommand support (subcommands are out of scope for v1).
 */
public fun main(argv: Array<String>) {
    val rest = if (argv.isEmpty()) argv else argv.copyOfRange(1, argv.size)
    when (argv.firstOrNull()) {
        "greet" -> runGreet(rest)
        else -> {
            System.err.println("Usage: sample <command> [options]")
            System.err.println()
            System.err.println("Commands:")
            System.err.println("  greet   Print a greeting (delegate style)")
        }
    }
}
