package org.draftcode.argot.samples

// #region declare
import org.draftcode.argot.Arguments

class CopyArgs : Arguments(programName = "copy", description = "Copy a file.") {
    val source: String by argument(help = "File to copy")
    val destination: String? by argument(help = "Where to put it").optional()
}
// #endregion declare

// #region use
fun destinationOf(args: CopyArgs): String = args.destination ?: "${args.source}.bak"
// #endregion use

// #region rest
class ArchiveArgs : Arguments(programName = "archive", description = "Archive files.") {
    val output: String by argument(help = "Archive to create")
    val inputs: List<String> by argument(help = "Files to include").multiple().required()
}
// #endregion rest
