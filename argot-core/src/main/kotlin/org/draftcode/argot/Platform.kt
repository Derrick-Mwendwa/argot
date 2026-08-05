package org.draftcode.argot

import kotlin.system.exitProcess

internal object Platform {
    fun printOut(text: String) {
        println(text)
    }

    fun printErr(text: String) {
        System.err.println(text)
    }

    fun exit(code: Int): Nothing {
        exitProcess(code)
    }
}
