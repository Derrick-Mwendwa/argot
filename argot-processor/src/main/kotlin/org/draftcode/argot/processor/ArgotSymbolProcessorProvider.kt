package org.draftcode.argot.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP entry point for Argot's annotation style. Registered via
 * `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider` and discovered by
 * KSP at compile time; it is never present on a consumer's runtime classpath.
 */
public class ArgotSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ArgotSymbolProcessor(environment.codeGenerator, environment.logger)
}
