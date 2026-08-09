<script setup lang="ts">
import { computed } from 'vue'
import { titleAnchor } from '../../api/anchors.mts'
import type { Classlike } from '../../api/model.mts'
import ApiMember from './ApiMember.vue'
import ApiSource from './ApiSource.vue'
import ApiTokens from './ApiTokens.vue'

const props = defineProps<{ cls: Classlike }>()

const packageUrl = computed(() => props.cls.url.slice(0, props.cls.url.lastIndexOf('/')))

const typeParams = computed(() =>
  props.cls.typeParameters?.length
    ? `<${props.cls.typeParameters.map((t) => t.name).join(', ')}>`
    : '',
)

const sections = computed(() =>
  [
    { title: 'Constructors', members: props.cls.constructors ?? [] },
    { title: 'Properties', members: props.cls.properties ?? [] },
    { title: 'Functions', members: props.cls.functions ?? [] },
  ].filter((s) => s.members.length > 0),
)
</script>

<template>
  <div class="api-page">
    <div class="api-eyebrow">
      <span class="api-kind">{{ cls.kind }}</span>
      <a class="api-pkg" :href="packageUrl">{{ cls.packageName }}</a>
    </div>

    <h1 :id="titleAnchor(cls.name)">
      {{ cls.name }}<span class="api-generics">{{ typeParams }}</span>
    </h1>

    <div v-if="cls.modifiers?.length || cls.supertypes?.length" class="api-meta">
      <span v-if="cls.modifiers?.length" class="api-modifiers">{{ cls.modifiers.join(' ') }}</span>
      <span v-if="cls.supertypes?.length" class="api-supertypes">
        <span class="api-meta-label">extends</span>
        <template v-for="(s, i) in cls.supertypes" :key="i">
          <ApiTokens :tokens="s.type" /><span v-if="i < cls.supertypes.length - 1">, </span>
        </template>
      </span>
    </div>

    <div v-if="cls.doc?.deprecated" class="api-deprecated">
      <strong>Deprecated.</strong> <span v-html="cls.doc.deprecated" />
    </div>

    <div v-if="cls.doc?.html" class="api-prose" v-html="cls.doc.html" />

    <ApiSource :url="cls.sourceUrl" :path="cls.source" />

    <template v-if="cls.entries?.length">
      <h2 id="entries" tabindex="-1">
        Entries<a class="header-anchor" href="#entries" aria-label="Permalink to Entries" />
      </h2>
      <dl class="api-entries">
        <template v-for="e in cls.entries" :key="e.id">
          <dt>{{ e.name }}</dt>
          <dd v-html="e.doc?.html ?? ''" />
        </template>
      </dl>
    </template>

    <template v-for="section in sections" :key="section.title">
      <h2 :id="section.title.toLowerCase()" tabindex="-1">
        {{ section.title }}
        <a
          class="header-anchor"
          :href="`#${section.title.toLowerCase()}`"
          :aria-label="`Permalink to ${section.title}`"
        />
      </h2>
      <ApiMember v-for="m in section.members" :key="m.id + m.anchor" :member="m" />
    </template>
  </div>
</template>

<style scoped>
.api-eyebrow {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.4rem;
}

.api-kind {
  padding: 0.1rem 0.5rem;
  border-radius: 999px;
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.api-pkg {
  font-family: var(--vp-font-family-mono);
  font-size: 0.78rem;
  color: var(--vp-c-text-3);
  text-decoration: none;
}

.api-pkg:hover {
  color: var(--vp-c-brand-1);
}

h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 650;
  letter-spacing: -0.02em;
}

.api-generics {
  font-family: var(--vp-font-family-mono);
  font-size: 0.65em;
  font-weight: 400;
  color: var(--vp-c-text-3);
}

.api-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.4rem 1rem;
  margin: 0.6rem 0 1rem;
  font-family: var(--vp-font-family-mono);
  font-size: 0.8rem;
  color: var(--vp-c-text-2);
}

.api-meta-label {
  margin-right: 0.4rem;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--vp-c-text-3);
}

h2 {
  margin: 2.5rem 0 0;
  padding-top: 1.5rem;
  border-top: 1px solid var(--vp-c-divider);
  font-size: 1.3rem;
  letter-spacing: -0.01em;
}

.api-entries {
  display: grid;
  grid-template-columns: minmax(5rem, auto) 1fr;
  gap: 0.4rem 1.5rem;
  margin: 1rem 0;
}

.api-entries dt {
  font-family: var(--vp-font-family-mono);
  font-size: 0.85rem;
  font-weight: 600;
}

.api-entries dd {
  margin: 0;
}

.api-entries :deep(p) {
  margin: 0;
}

.api-prose :deep(p:first-child) {
  margin-top: 0.75rem;
}

.api-deprecated {
  margin: 1rem 0;
  padding: 0.6rem 0.9rem;
  border: 1px solid var(--a-hairline);
  border-radius: var(--a-radius-chip);
  background: var(--vp-c-bg-soft);
}

/* The kind is carried by the label now that the card has a plain hairline. */
.api-deprecated strong {
  color: #b4611b;
}

:global(.dark) .api-deprecated strong {
  color: #dc9a5e;
}

.api-deprecated :deep(p) {
  margin: 0;
  display: inline;
}
</style>
