package org.draftcode.argot.processor

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Processor-level tests using the KSP2 compile-testing harness: one positive generation case and
 * several negative cases proving the compile-time validations report clear errors (§6.3, §8).
 */
@OptIn(ExperimentalCompilerApi::class)
class ArgotProcessorTest {

    private fun newCompilation(source: SourceFile): KotlinCompilation =
        KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            messageOutputStream = System.out
            useKsp2()
            configureKsp {
                symbolProcessorProviders += ArgotSymbolProcessorProvider()
            }
        }

    private fun compile(source: SourceFile): JvmCompilationResult = newCompilation(source).compile()

    @Test
    fun generatesParserForValidCommand() {
        val source = SourceFile.kotlin(
            "ServeArgs.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option
            import org.draftcode.argot.annotations.Flag
            import org.draftcode.argot.annotations.Argument

            @Command(name = "serve", description = "Run the server")
            data class ServeArgs(
                @Option(names = ["--host"], default = "0.0.0.0") val host: String,
                @Option(names = ["--port", "-p"]) val port: Int,
                @Flag(names = ["--verbose", "-v"]) val verbose: Boolean = false,
                @Argument val files: List<String>,
            )
            """.trimIndent(),
        )
        val compilation = newCompilation(source)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)

        val generated = compilation.workingDir.walkTopDown().firstOrNull { it.name == "parseServeArgs.kt" }
        assertTrue(generated != null && generated.exists(), "expected parseServeArgs.kt to be generated")
        val text = generated.readText()
        assertContains(text, "public fun parseServeArgs(argv: Array<String>): ServeArgs")
        assertContains(text, "ArgotEngine.parse(spec, argv)")
        assertContains(text, "required = true") // port is required
    }

    @Test
    fun duplicateNamesFailCompilation() {
        val source = SourceFile.kotlin(
            "DupArgs.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option
            import org.draftcode.argot.annotations.Flag

            @Command(name = "dup")
            data class DupArgs(
                @Option(names = ["--x"]) val a: Int,
                @Flag(names = ["--x"]) val b: Boolean = false,
            )
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "duplicate option/flag name '--x'")
    }

    @Test
    fun flagOnNonBooleanFailsCompilation() {
        val source = SourceFile.kotlin(
            "BadFlag.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Flag

            @Command(name = "bad")
            data class BadFlag(
                @Flag(names = ["--count"]) val count: Int,
            )
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "must be a non-nullable Boolean")
    }

    @Test
    fun unreadableKotlinDefaultFailsCompilation() {
        val source = SourceFile.kotlin(
            "KotlinDefault.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option

            @Command(name = "kd")
            data class KotlinDefault(
                @Option(names = ["--port"]) val port: Int = 8080,
            )
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "Kotlin default that Argot cannot read")
    }

    @Test
    fun internalCommandGeneratesInternalParser() {
        val source = SourceFile.kotlin(
            "Internal.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option

            @Command(name = "intl")
            internal data class IntlArgs(
                @Option(names = ["--x"]) val x: Int,
            )
            """.trimIndent(),
        )
        val compilation = newCompilation(source)
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
        val generated = compilation.workingDir.walkTopDown().firstOrNull { it.name == "parseIntlArgs.kt" }
        assertTrue(generated != null && generated.exists())
        assertContains(generated.readText(), "internal fun parseIntlArgs(")
    }

    @Test
    fun abstractCommandFailsCompilation() {
        val source = SourceFile.kotlin(
            "Abstract.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option

            @Command(name = "abs")
            abstract class AbstractArgs(
                @Option(names = ["--x"]) val x: Int,
            )
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "concrete")
    }

    @Test
    fun privateConstructorFailsCompilation() {
        val source = SourceFile.kotlin(
            "Priv.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option

            @Command(name = "priv")
            data class PrivArgs private constructor(
                @Option(names = ["--x"]) val x: Int,
            )
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "must be public or internal")
    }

    @Test
    fun sameSimpleNameCommandsFailWithCleanError() {
        val source = SourceFile.kotlin(
            "Collide.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option

            class Outer1 {
                @Command(name = "a")
                data class Args(@Option(names = ["--x"]) val x: Int)
            }
            class Outer2 {
                @Command(name = "b")
                data class Args(@Option(names = ["--y"]) val y: Int)
            }
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "clashes with another @Command")
    }

    @Test
    fun unsupportedTypeFailsCompilation() {
        val source = SourceFile.kotlin(
            "BadType.kt",
            """
            package com.example
            import org.draftcode.argot.annotations.Command
            import org.draftcode.argot.annotations.Option

            @Command(name = "bt")
            data class BadType(
                @Option(names = ["--when"]) val at: java.time.Instant,
            )
            """.trimIndent(),
        )
        val result = compile(source)
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, "unsupported type")
    }
}
