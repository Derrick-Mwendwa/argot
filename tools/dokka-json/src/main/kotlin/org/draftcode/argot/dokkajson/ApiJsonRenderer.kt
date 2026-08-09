package org.draftcode.argot.dokkajson

import java.io.File
import org.jetbrains.dokka.model.AdditionalModifiers
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.DAnnotation
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.DProperty
import org.jetbrains.dokka.model.DTypeParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.WithAbstraction
import org.jetbrains.dokka.model.WithGenerics
import org.jetbrains.dokka.model.WithSources
import org.jetbrains.dokka.model.WithSupertypes
import org.jetbrains.dokka.model.WithVisibility
import org.jetbrains.dokka.model.doc.Deprecated as DeprecatedTag
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.Param
import org.jetbrains.dokka.model.doc.Receiver
import org.jetbrains.dokka.model.doc.Return
import org.jetbrains.dokka.model.doc.Sample
import org.jetbrains.dokka.model.doc.See
import org.jetbrains.dokka.model.doc.Since
import org.jetbrains.dokka.model.doc.TagWrapper
import org.jetbrains.dokka.model.doc.Throws
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.renderers.Renderer

/**
 * Writes the API as `api.json` in Dokka's output directory instead of HTML, keeping Dokka's Kotlin
 * analysis and KDoc parsing while leaving presentation entirely to the site.
 */
internal class ApiJsonRenderer(private val context: DokkaContext) : Renderer {

    override fun render(root: RootPageNode) {
        val packages = ModuleCapture.of(context)?.packages.orEmpty().filter { it.hasContent() }

        // The multi-module aggregation step has no documentables of its own; writing an empty file
        // there would overwrite a good one.
        if (packages.isEmpty()) {
            context.logger.info("dokka-json: no packages in this generation, nothing written")
            return
        }

        val ownPackages = packages.map { it.packageName }.toSet()
        val model = ModelWriter(ownPackages)

        val json =
            jsonObject(
                // Bumped on incompatible shape changes so the site fails loudly rather than
                // rendering blanks from an older version tree.
                "schema" to SCHEMA_VERSION.json(),
                "module" to context.configuration.moduleName.json(),
                "version" to context.configuration.moduleVersion?.json(),
                "packages" to
                    JsonArray(packages.sortedBy { it.packageName }.map { model.pkg(it) }),
            )

        val outputDir = context.configuration.outputDir
        outputDir.mkdirs()
        val file = File(outputDir, "api.json")
        file.writeText(json.render())
        context.logger.info(
            "dokka-json: wrote ${file.absolutePath} (${packages.size} packages)"
        )
    }

    private fun DPackage.hasContent(): Boolean =
        classlikes.isNotEmpty() || functions.isNotEmpty() || properties.isNotEmpty()

    private companion object {
        const val SCHEMA_VERSION = 1
    }
}

/** Turns the documentable model into the emitted shape. */
private class ModelWriter(private val ownPackages: Set<String>) {
    private val types = TypeRenderer(ownPackages)
    private val docs = DocRenderer(ownPackages)

    fun pkg(pkg: DPackage): Json {
        val classlikes = pkg.classlikes.sortedBy { it.name.orEmpty() }
        val functions = withStableIds(pkg.functions.sortedBy { it.name })
        val properties = withStableIds(pkg.properties.sortedBy { it.name })
        return jsonObject(
            "name" to pkg.packageName.json(),
            "id" to pkg.packageName.json(),
            "doc" to pkg.doc(),
            "classlikes" to classlikes.jsonOrNull { classlike(it) },
            "functions" to functions.jsonOrNull { (id, f) -> callable(id, f) },
            "properties" to properties.jsonOrNull { (id, p) -> property(id, p) },
        )
    }

    private fun classlike(c: DClasslike): Json {
        val constructors =
            withStableIds((c as? DClass)?.constructors.orEmpty().sortedBy { it.parameters.size })
        val functions = withStableIds(c.functions.sortedBy { it.name })
        val properties = withStableIds(c.properties.sortedBy { it.name })
        val entries = (c as? DEnum)?.entries.orEmpty()

        return jsonObject(
            "kind" to c.kind().json(),
            "id" to c.dri.id().json(),
            "name" to c.name.orEmpty().json(),
            "packageName" to c.dri.packageName.orEmpty().json(),
            "modifiers" to c.modifiers().jsonOrNull { it.json() },
            "typeParameters" to (c as? WithGenerics)?.generics.orEmpty().jsonOrNull(::typeParameter),
            "supertypes" to
                (c as? WithSupertypes)
                    ?.supertypes
                    ?.values
                    ?.flatten()
                    .orEmpty()
                    .distinctBy { it.typeConstructor.dri }
                    .jsonOrNull { s ->
                        jsonObject(
                            "type" to JsonArray(types.render(s.typeConstructor).map { it.toJson() }),
                            "kind" to s.kind.toString().lowercase().json(),
                        )
                    },
            "doc" to c.doc(),
            "entries" to
                entries.jsonOrNull { e ->
                    jsonObject(
                        "id" to e.dri.id().json(),
                        "name" to e.name.json(),
                        "doc" to e.doc(),
                    )
                },
            "constructors" to constructors.jsonOrNull { (id, f) -> callable(id, f) },
            "functions" to functions.jsonOrNull { (id, f) -> callable(id, f) },
            "properties" to properties.jsonOrNull { (id, p) -> property(id, p) },
            "source" to c.sourcePath()?.json(),
        )
    }

    private fun callable(id: String, f: DFunction): Json =
        jsonObject(
            "kind" to (if (f.isConstructor) "constructor" else "function").json(),
            "id" to id.json(),
            "name" to f.name.json(),
            "modifiers" to f.modifiers().jsonOrNull { it.json() },
            "typeParameters" to f.generics.jsonOrNull(::typeParameter),
            "receiver" to f.receiver?.let { r -> JsonArray(types.render(r.type).map { it.toJson() }) },
            "parameters" to
                f.parameters.jsonOrNull { p ->
                    jsonObject(
                        "name" to p.name.orEmpty().json(),
                        "type" to JsonArray(types.render(p.type).map { it.toJson() }),
                        "doc" to f.paramDoc(p),
                    )
                },
            // A constructor's return type is the class the signature already names.
            "returnType" to
                if (f.isConstructor) null
                else JsonArray(types.render(f.type).map { it.toJson() }),
            "signature" to f.signatureText().json(),
            "doc" to f.doc(),
            "source" to f.sourcePath()?.json(),
        )

    private fun property(id: String, p: DProperty): Json =
        jsonObject(
            "kind" to "property".json(),
            "id" to id.json(),
            "name" to p.name.json(),
            "modifiers" to p.modifiers().jsonOrNull { it.json() },
            "receiver" to p.receiver?.let { r -> JsonArray(types.render(r.type).map { it.toJson() }) },
            "type" to JsonArray(types.render(p.type).map { it.toJson() }),
            "mutable" to (p.setter != null).json(),
            "signature" to p.signatureText().json(),
            "doc" to p.doc(),
            "source" to p.sourcePath()?.json(),
        )

    private fun typeParameter(t: DTypeParameter): Json =
        jsonObject(
            "name" to t.name.json(),
            "bounds" to
                t.bounds
                    .filterNot { types.plainText(it) == "Any?" }
                    .jsonOrNull { b -> JsonArray(types.render(b).map { it.toJson() }) },
        )

    // ---- documentation -------------------------------------------------------------------------

    private fun Documentable.doc(): Json? {
        val tags = documentation.values.flatMap { it.children }
        if (tags.isEmpty()) return null

        val description = tags.filterIsInstance<Description>().firstOrNull()
        val html = description?.let { docs.render(it.root) }.orEmpty()

        return jsonObject(
            "html" to html.takeIf { it.isNotBlank() }?.json(),
            // Computed here so member lists and search results agree on where the summary ends.
            "summary" to
                description?.let { docs.plainText(it.root).firstSentence() }?.takeIf {
                    it.isNotBlank()
                }?.json(),
            "returns" to tags.firstOfType<Return>()?.let { docs.render(it.root) }?.json(),
            "receiver" to tags.firstOfType<Receiver>()?.let { docs.render(it.root) }?.json(),
            "since" to tags.firstOfType<Since>()?.let { docs.plainText(it.root) }?.json(),
            "deprecated" to tags.firstOfType<DeprecatedTag>()?.let { docs.render(it.root) }?.json(),
            "throws" to
                tags.filterIsInstance<Throws>().jsonOrNull { t ->
                    jsonObject(
                        "type" to t.name.json(),
                        "ref" to
                            t.exceptionAddress
                                ?.takeIf { it.isLocal(ownPackages) && it.pointsToDeclaration() }
                                ?.id()
                                ?.json(),
                        "html" to docs.render(t.root).json(),
                    )
                },
            "see" to
                tags.filterIsInstance<See>().jsonOrNull { s ->
                    jsonObject(
                        "name" to s.name.json(),
                        "ref" to
                            s.address
                                ?.takeIf { it.isLocal(ownPackages) && it.pointsToDeclaration() }
                                ?.id()
                                ?.json(),
                        "html" to docs.render(s.root).takeIf { it.isNotBlank() }?.json(),
                    )
                },
            "samples" to tags.filterIsInstance<Sample>().jsonOrNull { it.name.json() },
        )
            .takeIf { it.hasEntries() }
    }

    private fun DFunction.paramDoc(p: DParameter): Json? =
        documentation.values
            .flatMap { it.children }
            .filterIsInstance<Param>()
            .firstOrNull { it.name == p.name }
            ?.let { docs.render(it.root) }
            ?.takeIf { it.isNotBlank() }
            ?.json()

    // ---- signatures ----------------------------------------------------------------------------

    private fun DFunction.signatureText(): String = buildString {
        modifiers().forEach { append(it).append(' ') }
        if (!isConstructor) append("fun ")
        if (generics.isNotEmpty()) {
            append('<')
            append(generics.joinToString(", ") { it.name })
            append("> ")
        }
        receiver?.let { append(types.plainText(it.type)).append('.') }
        append(name)
        append('(')
        append(parameters.joinToString(", ") { "${it.name}: ${types.plainText(it.type)}" })
        append(')')
        if (!isConstructor) append(": ").append(types.plainText(type))
    }

    private fun DProperty.signatureText(): String = buildString {
        modifiers().forEach { append(it).append(' ') }
        append(if (setter != null) "var " else "val ")
        receiver?.let { append(types.plainText(it.type)).append('.') }
        append(name)
        append(": ")
        append(types.plainText(type))
    }

    // ---- modifiers, kinds, sources -------------------------------------------------------------

    /** `final` is dropped: it is Kotlin's default and repeating it buries the modifiers that matter. */
    private fun Documentable.modifiers(): List<String> {
        val visibility =
            (this as? WithVisibility)
                ?.visibility
                ?.values
                ?.firstOrNull()
                ?.name
                ?.takeIf { it.isNotBlank() && it != "public" }

        val abstraction =
            (this as? WithAbstraction)
                ?.modifier
                ?.values
                ?.firstOrNull()
                ?.name
                ?.takeIf { it.isNotBlank() && it != "final" && it != "empty" }

        // Sound for any Documentable, since AdditionalModifiers is an ExtraProperty<Documentable>;
        // the star projection `as?` would otherwise infer is what stops the key type-checking.
        @Suppress("UNCHECKED_CAST")
        val extras =
            (this as? WithExtraProperties<Documentable>)
                ?.extra
                ?.allOfType<AdditionalModifiers>()
                .orEmpty()
                .flatMap { it.content.values.flatten() }
                .map { it.name }
                .filter { it != "final" }

        return (listOfNotNull(visibility, abstraction) + extras).distinct()
    }

    private fun DClasslike.kind(): String =
        when (this) {
            is DClass -> "class"
            is DInterface -> "interface"
            is DObject -> "object"
            is DEnum -> "enum"
            is DAnnotation -> "annotation"
        }

    /**
     * A repo-relative path rather than a URL: the URL depends on which tag the site is building,
     * which this module cannot know.
     *
     * Cut at the module directory containing `src/`, since the absolute prefix is wherever the build
     * happened to run and would otherwise be published.
     */
    private fun Documentable.sourcePath(): String? {
        val path = (this as? WithSources)?.sources?.values?.firstOrNull()?.path ?: return null
        val src = path.lastIndexOf("/src/")
        if (src == -1) return path
        val moduleStart = path.lastIndexOf('/', src - 1)
        return if (moduleStart == -1) path else path.substring(moduleStart + 1)
    }

    /** Callers must pass a sorted list: the order decides which overload owns the unsuffixed id. */
    private fun <T : Documentable> withStableIds(items: List<T>): List<Pair<String, T>> =
        disambiguate(items.map { it.dri.id() }).zip(items)
}

private inline fun <reified T : TagWrapper> List<TagWrapper>.firstOfType(): T? =
    filterIsInstance<T>().firstOrNull()

private fun String.firstSentence(): String {
    val end = indexOfFirst { it == '.' }
    return if (end == -1) this else substring(0, end + 1)
}

private fun JsonObject.hasEntries(): Boolean = render() != "{}"
