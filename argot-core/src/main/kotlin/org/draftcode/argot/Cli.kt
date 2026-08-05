package org.draftcode.argot

/**
 * Runs [block] and turns Argot's outcomes into the usual command-line behaviour:
 * - success returns the parsed value;
 * - `--help` and `--version` print to stdout and exit `0`;
 * - invalid input prints the usage line and `error: …` to stderr and exits `2`.
 *
 * ```
 * fun main(argv: Array<String>) {
 *     val args = cli { parseServeArgs(argv) }   // generated parser, or ServerArgs().parse(argv)
 *     runServer(args)
 * }
 * ```
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
