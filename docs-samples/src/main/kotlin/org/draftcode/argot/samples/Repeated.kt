package org.draftcode.argot.samples

// #region declare
import org.draftcode.argot.Arguments

class TagArgs : Arguments(programName = "tag", description = "Apply tags to a build.") {
    val tag: List<String> by option("--tag", "-t", help = "Tag to apply").multiple()
    val env: List<String> by option("--env", "-e", help = "Environment").multiple().required()
}
// #endregion declare

// #region use
fun summarise(args: TagArgs): String =
    "tags=${args.tag.joinToString(",")} envs=${args.env.joinToString(",")}"
// #endregion use

// #region typed
class PortArgs : Arguments(programName = "expose", description = "Expose ports.") {
    val port: List<Int> by option("--port", "-p", help = "Port to expose").int().multiple()
}
// #endregion typed
