package org.draftcode.argot.sample

/** Sample entry point: `greet` demonstrates the delegate style, `serve` the annotation style. */
fun main(args: Array<String>) {
    val rest = if (args.isEmpty()) args else args.copyOfRange(1, args.size)
    when (args.firstOrNull()) {
        "greet" -> runGreet(rest)
        "serve" -> runServe(rest)
        else -> {
            System.err.println("Usage: sample <command> [options]")
            System.err.println()
            System.err.println("Commands:")
            System.err.println("  greet   Print a greeting (delegate style)")
            System.err.println("  serve   Run the server (annotation style)")
        }
    }
}
