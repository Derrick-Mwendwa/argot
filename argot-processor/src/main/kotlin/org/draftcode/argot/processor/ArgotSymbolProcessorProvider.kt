package org.draftcode.argot.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/** KSP entry point for Argot's annotation style, registered via `META-INF/services`. */
public class ArgotSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ArgotSymbolProcessor(environment.codeGenerator, environment.logger)
}
