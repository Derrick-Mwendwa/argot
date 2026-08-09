package org.draftcode.argot.dokkajson

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.PointingToDeclaration

/**
 * Stable identifiers for declarations. The site maps these to URLs; this module does not, so route
 * changes never mean regenerating published JSON.
 *
 *   org.draftcode.argot                     package
 *   org.draftcode.argot/ParsedValues        classlike
 *   org.draftcode.argot/ParsedValues#flag   member of a classlike
 *   org.draftcode.argot#parsed              top-level member
 *
 * `#` separates the member so a top-level function can never collide with a classlike of the same
 * name.
 */
internal fun DRI.id(): String {
    val pkg = packageName.orEmpty()
    val classes = classNames
    val member = callable?.name
    return buildString {
        append(pkg)
        if (classes != null) {
            append('/')
            append(classes)
        }
        if (member != null) {
            append('#')
            append(member)
        }
    }
}

/** True for declarations belonging to the library itself, which are the ones we can link to. */
internal fun DRI.isLocal(ownPackages: Set<String>): Boolean = packageName in ownPackages

/**
 * True when the DRI names a declaration rather than one of its parameters or type parameters.
 *
 * A KDoc `[name]` referring to a parameter carries the enclosing function's DRI with a target, so
 * without this check every `[param]` becomes a link to the function it appears on.
 */
internal fun DRI.pointsToDeclaration(): Boolean = target is PointingToDeclaration

/**
 * Suffixes overloads, which share an [id] and would otherwise share an anchor. Callers must pass a
 * stable order, since that is what decides which overload keeps the unsuffixed id.
 */
internal fun disambiguate(ids: List<String>): List<String> {
    val counts = ids.groupingBy { it }.eachCount()
    val seen = mutableMapOf<String, Int>()
    return ids.map { id ->
        if (counts.getValue(id) == 1) {
            id
        } else {
            val n = seen.getOrDefault(id, 0)
            seen[id] = n + 1
            "$id~$n"
        }
    }
}
