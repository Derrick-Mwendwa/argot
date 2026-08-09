package org.draftcode.argot.samples

// #region declare
import org.draftcode.argot.Arguments

enum class Level { DEBUG, INFO, WARN, ERROR }

class LogArgs : Arguments(programName = "log", description = "Read the log.") {
    val level: Level by option("--level", "-l", help = "Minimum level")
        .enum<Level>()
        .default(Level.INFO)
}
// #endregion declare

// #region use
fun atLeast(args: LogArgs): List<Level> = Level.entries.filter { it >= args.level }
// #endregion use
