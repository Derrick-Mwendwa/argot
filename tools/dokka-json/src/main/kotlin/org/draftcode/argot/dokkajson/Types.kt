package org.draftcode.argot.dokkajson

import org.jetbrains.dokka.model.Bound
import org.jetbrains.dokka.model.Contravariance
import org.jetbrains.dokka.model.Covariance
import org.jetbrains.dokka.model.DefinitelyNonNullable
import org.jetbrains.dokka.model.Dynamic
import org.jetbrains.dokka.model.FunctionalTypeConstructor
import org.jetbrains.dokka.model.GenericTypeConstructor
import org.jetbrains.dokka.model.Invariance
import org.jetbrains.dokka.model.JavaObject
import org.jetbrains.dokka.model.Nullable
import org.jetbrains.dokka.model.PrimitiveJavaType
import org.jetbrains.dokka.model.Projection
import org.jetbrains.dokka.model.Star
import org.jetbrains.dokka.model.TypeAliased
import org.jetbrains.dokka.model.TypeConstructor
import org.jetbrains.dokka.model.TypeParameter
import org.jetbrains.dokka.model.UnresolvedBound
import org.jetbrains.dokka.model.Variance
import org.jetbrains.dokka.model.Void

/**
 * One piece of a rendered type. Types are token lists rather than strings so the site can link the
 * identifiers without re-parsing Kotlin's type syntax to find them.
 */
internal data class TypeToken(
    val text: String,
    /** Identifier of the declaration this names, when it is one of ours. */
    val ref: String? = null,
    /** Fully qualified name of a type from outside the library, for tooltips and future linking. */
    val external: String? = null,
) {
    fun toJson(): Json =
        jsonObject(
            "text" to text.json(),
            "ref" to ref?.json(),
            "external" to external?.json(),
        )
}

/** Renders Dokka's type model into tokens. [ownPackages] decides what counts as linkable. */
internal class TypeRenderer(private val ownPackages: Set<String>) {

    fun render(bound: Bound): List<TypeToken> = buildList { append(bound) }

    fun plainText(bound: Bound): String = render(bound).joinToString("") { it.text }

    private fun MutableList<TypeToken>.append(projection: Projection) {
        when (projection) {
            is Star -> add(TypeToken("*"))
            is Variance<*> -> {
                val keyword =
                    when (projection) {
                        is Covariance<*> -> "out "
                        is Contravariance<*> -> "in "
                        is Invariance<*> -> ""
                    }
                if (keyword.isNotEmpty()) add(TypeToken(keyword))
                append(projection.inner)
            }
            is Bound -> append(projection)
        }
    }

    private fun MutableList<TypeToken>.append(bound: Bound) {
        when (bound) {
            is Nullable -> {
                append(bound.inner)
                add(TypeToken("?"))
            }
            is DefinitelyNonNullable -> {
                append(bound.inner)
                add(TypeToken(" & Any"))
            }
            is TypeParameter -> add(TypeToken(bound.presentableName ?: bound.name))
            is PrimitiveJavaType -> add(TypeToken(bound.name))
            is UnresolvedBound -> add(TypeToken(bound.name))
            is JavaObject -> add(TypeToken("Any", external = "kotlin.Any"))
            is Void -> add(TypeToken("Unit", external = "kotlin.Unit"))
            is Dynamic -> add(TypeToken("dynamic"))
            // The alias, not its expansion: the alias is what the signature says.
            is TypeAliased -> append(bound.typeAlias)
            is FunctionalTypeConstructor -> appendFunctional(bound)
            is GenericTypeConstructor -> appendConstructor(bound)
        }
    }

    private fun MutableList<TypeToken>.appendConstructor(type: TypeConstructor) {
        val fqName = type.dri.let { dri ->
            listOfNotNull(dri.packageName?.takeIf { it.isNotEmpty() }, dri.classNames)
                .joinToString(".")
        }
        val simpleName = type.dri.classNames?.substringAfterLast('.') ?: fqName
        val local = type.dri.isLocal(ownPackages)
        add(
            TypeToken(
                text = simpleName,
                ref = if (local) type.dri.id() else null,
                external = if (local) null else fqName.takeIf { it.isNotEmpty() },
            )
        )
        appendProjections(type.projections)
    }

    private fun MutableList<TypeToken>.appendFunctional(type: FunctionalTypeConstructor) {
        // Dokka models `(A) -> B` as a FunctionN whose last projection is the return type.
        val projections = type.projections
        if (projections.isEmpty()) {
            appendConstructor(type)
            return
        }
        if (type.isSuspendable) add(TypeToken("suspend "))
        val params = projections.dropLast(1)
        val returnType = projections.last()

        val receiverCount = if (type.isExtensionFunction && params.isNotEmpty()) 1 else 0
        if (receiverCount == 1) {
            append(params.first())
            add(TypeToken("."))
        }
        add(TypeToken("("))
        params.drop(receiverCount).forEachIndexed { i, p ->
            if (i > 0) add(TypeToken(", "))
            append(p)
        }
        add(TypeToken(") -> "))
        append(returnType)
    }

    private fun MutableList<TypeToken>.appendProjections(projections: List<Projection>) {
        if (projections.isEmpty()) return
        add(TypeToken("<"))
        projections.forEachIndexed { i, p ->
            if (i > 0) add(TypeToken(", "))
            append(p)
        }
        add(TypeToken(">"))
    }
}
