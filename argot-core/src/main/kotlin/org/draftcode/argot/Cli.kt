package org.draftcode.argot

/**
 * Runs a parse [block] and adapts Argot's pure outcomes to a conventional CLI process lifecycle.
 *
 * This is the only convenience that touches stdout/stderr and the process exit code, keeping the
 * engine itself testable. Behavior:
 * - On success: returns the parsed value.
 * - On [HelpRequested]: prints the help text to **stdout** and exits `0`.
 * - On [VersionRequested]: prints the version line to **stdout** and exits `0`.
 * - On [ArgotParseException]: prints the usage line and an `error: …` message to **stderr** and
 *   exits `2`.
 *
 * Example:
 * ```
 * fun main(argv: Array<String>) {
 *     val args = cli { parseServeArgs(argv) }   // generated parser, or ServerArgs().parse(argv)
 *     runServer(args)
 * }
 * ```
 *
 * @param block the parse to run; typically a generated `parseX(argv)` call or a delegate
 *   `Arguments.parse(argv)`.
 * @return the parsed value when parsing succeeds.
 */
public fun <T> cli(block: () -> T): T =
    try {
        block()
    } catch (e: HelpRequested) {
        Platform.printOut(e.rendered)
        Platform.exit(0)
    } catch (e: VersionRequested) {
        Platform.printOut(e.rendered)
        Platform.exit(0)
    } catch (e: ArgotParseException) {
        e.usage?.let { Platform.printErr(it) }
        Platform.printErr("error: ${e.message}")
        Platform.exit(2)
    }
