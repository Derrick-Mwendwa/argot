package org.draftcode.argot

import kotlin.system.exitProcess

/**
 * The single place where Argot touches JVM-only side effects: stdout, stderr, and process exit.
 *
 * The parsing engine is pure and never references this object, which keeps the core path free of
 * JVM-only APIs and testable without capturing streams or trapping `exitProcess`. A future Kotlin
 * Multiplatform migration would replace this with an `expect`/`actual` declaration; nothing else
 * in the engine would change.
 */
internal object Platform {
    /** Writes [text] followed by a newline to standard output. */
    fun printOut(text: String) {
        println(text)
    }

    /** Writes [text] followed by a newline to standard error. */
    fun printErr(text: String) {
        System.err.println(text)
    }

    /** Terminates the process with [code]. */
    fun exit(code: Int): Nothing {
        exitProcess(code)
    }
}
