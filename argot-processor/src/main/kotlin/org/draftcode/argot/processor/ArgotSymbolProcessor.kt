package org.draftcode.argot.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import org.draftcode.argot.annotations.NO_DEFAULT

private const val COMMAND_FQN = "org.draftcode.argot.annotations.Command"
private const val OPTION_FQN = "org.draftcode.argot.annotations.Option"
private const val FLAG_FQN = "org.draftcode.argot.annotations.Flag"
private const val ARGUMENT_FQN = "org.draftcode.argot.annotations.Argument"
private val ARGOT_PARAM_ANNOTATIONS = setOf(OPTION_FQN, FLAG_FQN, ARGUMENT_FQN)

internal enum class ParamKind { OPTION, FLAG, ARGUMENT }

internal class ResolvedParam(
    val node: KSValueParameter,
    val paramName: String,
    val kind: ParamKind,
    val names: List<String>,
    val help: String,
    val converter: CodeBlock?,
    val multiple: Boolean,
    val required: Boolean,
    val default: String?,
) {
    val lookupKey: String get() = if (kind == ParamKind.ARGUMENT) paramName else names.first()
}

internal class ArgotSymbolProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    private val codegen = CommandCodeGenerator(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(COMMAND_FQN).toList()
        val deferred = symbols.filterNot { it.validate() }
        val commands = symbols.filterIsInstance<KSClassDeclaration>().filter { it.validate() }

        // Report collisions here; otherwise the code generator fails with FileAlreadyExistsException.
        val byTarget = commands.groupBy { "${it.packageName.asString()}/parse${it.simpleName.asString()}" }
        commands.forEach { cls ->
            val key = "${cls.packageName.asString()}/parse${cls.simpleName.asString()}"
            if (byTarget.getValue(key).size > 1) {
                logger.error(
                    "@Command '${cls.simpleName.asString()}' would generate 'parse${cls.simpleName.asString()}', " +
                        "which clashes with another @Command of the same simple name in this package; rename one.",
                    cls,
                )
            } else {
                processCommand(cls)
            }
        }
        return deferred
    }

    private fun processCommand(cls: KSClassDeclaration) {
        val errors = ErrorSink(logger)
        val name = cls.simpleName.asString()

        if (cls.classKind != ClassKind.CLASS ||
            Modifier.ABSTRACT in cls.modifiers ||
            Modifier.SEALED in cls.modifiers ||
            Modifier.INNER in cls.modifiers
        ) {
            errors.report(
                "@Command must be applied to a concrete, non-abstract, non-sealed, non-inner class; '$name' is not one",
                cls,
            )
            return
        }
        if (Modifier.PRIVATE in cls.modifiers || Modifier.PROTECTED in cls.modifiers) {
            errors.report("@Command class '$name' must be public or internal so the generated parser can reference it", cls)
            return
        }

        val command = cls.annotations.first { it.fqn() == COMMAND_FQN }
        val programName = command.stringArg("name", "").ifBlank { name }
        val description = command.stringArg("description", "")

        val constructor = cls.primaryConstructor
        val parameters = constructor?.parameters.orEmpty()
        if (parameters.isEmpty()) {
            errors.report("@Command class '$name' must have a primary constructor with at least one parameter", cls)
            return
        }
        if (Modifier.PRIVATE in constructor!!.modifiers || Modifier.PROTECTED in constructor.modifiers) {
            errors.report("the primary constructor of '$name' must be public or internal so the generated parser can call it", constructor)
            return
        }

        val resolved = parameters.mapNotNull { resolveParam(it, errors) }
        validateCommand(cls, resolved, errors)

        if (errors.any) return
        codegen.generate(cls, programName, description, resolved, internalVisibility = Modifier.INTERNAL in cls.modifiers)
    }

    private fun resolveParam(param: KSValueParameter, errors: ErrorSink): ResolvedParam? {
        val paramName = param.name?.asString()
            ?: run { errors.report("a constructor parameter is missing a name", param); return null }
        val annotations = param.annotations.filter { it.fqn() in ARGOT_PARAM_ANNOTATIONS }.toList()
        when {
            annotations.isEmpty() -> {
                errors.report("parameter '$paramName' must be annotated with @Option, @Flag, or @Argument", param)
                return null
            }
            annotations.size > 1 -> {
                errors.report("parameter '$paramName' carries more than one of @Option/@Flag/@Argument", param)
                return null
            }
        }
        val annotation = annotations.single()
        val type = param.type.resolve()
        val nullable = type.isMarkedNullable
        return when (annotation.fqn()) {
            FLAG_FQN -> resolveFlag(param, paramName, annotation, type, nullable, errors)
            OPTION_FQN -> resolveOption(param, paramName, annotation, type, nullable, errors)
            ARGUMENT_FQN -> resolveArgument(param, paramName, annotation, type, nullable, errors)
            else -> null
        }
    }

    private fun resolveFlag(
        param: KSValueParameter,
        paramName: String,
        annotation: KSAnnotation,
        type: KSType,
        nullable: Boolean,
        errors: ErrorSink,
    ): ResolvedParam? {
        if (type.declaration.qualifiedName?.asString() != "kotlin.Boolean" || nullable) {
            errors.report("@Flag parameter '$paramName' must be a non-nullable Boolean", param)
            return null
        }
        val names = annotation.stringArrayArg("names")
        if (!validNames(names, paramName, "flag", param, errors)) return null
        return ResolvedParam(
            node = param,
            paramName = paramName,
            kind = ParamKind.FLAG,
            names = names,
            help = annotation.stringArg("help", ""),
            converter = null,
            multiple = false,
            required = false,
            default = null,
        )
    }

    private fun resolveOption(
        param: KSValueParameter,
        paramName: String,
        annotation: KSAnnotation,
        type: KSType,
        nullable: Boolean,
        errors: ErrorSink,
    ): ResolvedParam? {
        val names = annotation.stringArrayArg("names")
        if (!validNames(names, paramName, "option", param, errors)) return null
        val help = annotation.stringArg("help", "")
        val annoDefault = annotation.stringArg("default", NO_DEFAULT).takeUnless { it == NO_DEFAULT }

        if (isListType(type)) {
            if (annoDefault != null) {
                errors.report("@Option '$paramName' is repeatable (List) and cannot declare a default", param)
                return null
            }
            val element = listElementType(type)
                ?: run { errors.report("@Option '$paramName': cannot resolve the List element type", param); return null }
            val converter = resolveConverter(element)
                ?: run { errors.report(unsupportedType(paramName, element), param); return null }
            return ResolvedParam(
                param, paramName, ParamKind.OPTION, names, help, converter,
                multiple = true, required = false, default = null,
            )
        }

        val converter = resolveConverter(type)
            ?: run { errors.report(unsupportedType(paramName, type), param); return null }
        if (!nullable && annoDefault == null && param.hasDefault) {
            errors.report(
                "@Option '$paramName' has a Kotlin default that Argot cannot read. " +
                    "Add default=\"...\" to the annotation, or make the parameter type nullable.",
                param,
            )
            return null
        }
        val required = !nullable && annoDefault == null
        return ResolvedParam(
            param, paramName, ParamKind.OPTION, names, help, converter,
            multiple = false, required = required, default = annoDefault,
        )
    }

    private fun resolveArgument(
        param: KSValueParameter,
        paramName: String,
        annotation: KSAnnotation,
        type: KSType,
        nullable: Boolean,
        errors: ErrorSink,
    ): ResolvedParam? {
        val help = annotation.stringArg("help", "")

        if (isListType(type)) {
            val element = listElementType(type)
                ?: run { errors.report("@Argument '$paramName': cannot resolve the List element type", param); return null }
            val converter = resolveConverter(element)
                ?: run { errors.report(unsupportedType(paramName, element), param); return null }
            return ResolvedParam(
                param, paramName, ParamKind.ARGUMENT, names = emptyList(), help = help, converter = converter,
                multiple = true, required = false, default = null,
            )
        }

        val converter = resolveConverter(type)
            ?: run { errors.report(unsupportedType(paramName, type), param); return null }
        if (!nullable && param.hasDefault) {
            errors.report(
                "@Argument '$paramName' has a Kotlin default that Argot cannot read. " +
                    "Make the parameter type nullable to mark it optional.",
                param,
            )
            return null
        }
        return ResolvedParam(
            param, paramName, ParamKind.ARGUMENT, names = emptyList(), help = help, converter = converter,
            multiple = false, required = !nullable, default = null,
        )
    }

    private fun validateCommand(cls: KSClassDeclaration, resolved: List<ResolvedParam>, errors: ErrorSink) {
        val seen = mutableSetOf<String>()
        resolved.filter { it.kind != ParamKind.ARGUMENT }.forEach { param ->
            param.names.forEach { name ->
                if (!seen.add(name)) errors.report("duplicate option/flag name '$name'", param.node)
            }
        }

        val positionals = resolved.filter { it.kind == ParamKind.ARGUMENT }
        if (positionals.count { it.multiple } > 1) {
            errors.report("at most one List (trailing) positional is allowed", cls)
        }
        var sawOptionalOrMultiple = false
        positionals.forEachIndexed { index, arg ->
            if (arg.multiple && index != positionals.lastIndex) {
                errors.report("the List positional '${arg.paramName}' must be declared last", arg.node)
            }
            if (sawOptionalOrMultiple && arg.required && !arg.multiple) {
                errors.report("required positional '${arg.paramName}' cannot follow an optional or List positional", arg.node)
            }
            if (!arg.required || arg.multiple) sawOptionalOrMultiple = true
        }
    }

    private fun validNames(
        names: List<String>,
        paramName: String,
        kind: String,
        node: KSNode,
        errors: ErrorSink,
    ): Boolean {
        if (names.isEmpty()) {
            errors.report("$kind '$paramName' must declare at least one name", node)
            return false
        }
        var ok = true
        names.forEach { name ->
            when {
                name.isBlank() -> { errors.report("$kind '$paramName' has a blank name", node); ok = false }
                !name.startsWith("-") -> { errors.report("$kind name '$name' on '$paramName' must start with '-'", node); ok = false }
            }
        }
        return ok
    }

    private fun unsupportedType(paramName: String, type: KSType): String =
        "parameter '$paramName' has unsupported type '${type.declaration.qualifiedName?.asString()}'. " +
            "Supported: String, Int, Long, Double, Boolean, an enum, or a List of those."
}

private class ErrorSink(private val logger: KSPLogger) {
    var any: Boolean = false
        private set

    fun report(message: String, node: KSNode) {
        logger.error(message, node)
        any = true
    }
}

private fun KSAnnotation.fqn(): String? =
    annotationType.resolve().declaration.qualifiedName?.asString()

private fun KSAnnotation.stringArg(name: String, default: String): String =
    arguments.firstOrNull { it.name?.asString() == name }?.value as? String ?: default

private fun KSAnnotation.stringArrayArg(name: String): List<String> =
    (arguments.firstOrNull { it.name?.asString() == name }?.value as? List<*>)
        ?.mapNotNull { it as? String }
        ?: emptyList()
