package org.draftcode.argot.dokkajson

import java.util.Collections
import java.util.WeakHashMap
import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

/**
 * Replaces Dokka's HTML renderer with one that emits JSON. This overrides rather than adds: Dokka
 * permits one renderer, and registering a second fails the generation.
 */
public class ArgotJsonPlugin : DokkaPlugin() {

    private val dokkaBase by lazy { plugin<DokkaBase>() }

    @Suppress("unused")
    public val documentableCapture: org.jetbrains.dokka.plugability.Extension<*, *, *> by extending {
        CoreExtensions.documentableTransformer with ModuleCapture
    }

    @Suppress("unused")
    public val jsonRenderer: org.jetbrains.dokka.plugability.Extension<*, *, *> by extending {
        CoreExtensions.renderer providing ::ApiJsonRenderer override dokkaBase.htmlRenderer
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement =
        PluginApiPreviewAcknowledgement
}

/**
 * Hands the merged documentable model to the renderer.
 *
 * The renderer only receives the page tree, whose `documentable` accessor Dokka has deprecated, and
 * reconstructing the model from pages means re-merging what Dokka already merged. A transformer sees
 * the finished model directly, so this captures it and returns it untouched.
 */
internal object ModuleCapture : DocumentableTransformer {
    private val byContext: MutableMap<DokkaContext, DModule> =
        Collections.synchronizedMap(WeakHashMap())

    override fun invoke(original: DModule, context: DokkaContext): DModule {
        byContext[context] = original
        return original
    }

    fun of(context: DokkaContext): DModule? = byContext[context]
}
