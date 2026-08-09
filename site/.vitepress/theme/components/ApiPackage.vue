<script setup lang="ts">
import { computed } from 'vue'
import { titleAnchor } from '../../api/anchors.mts'
import type { ApiPackage } from '../../api/model.mts'
import ApiMember from './ApiMember.vue'

const props = defineProps<{ pkg: ApiPackage }>()

/** Grouped by kind so a reader scanning for "the interfaces" is not reading an alphabet. */
const groups = computed(() => {
  const order = ['class', 'interface', 'object', 'enum', 'annotation'] as const
  const labels: Record<string, string> = {
    class: 'Classes',
    interface: 'Interfaces',
    object: 'Objects',
    enum: 'Enums',
    annotation: 'Annotations',
  }
  return order
    .map((kind) => ({
      kind,
      label: labels[kind],
      items: props.pkg.classlikes.filter((c) => c.kind === kind),
    }))
    .filter((g) => g.items.length > 0)
})

const topLevel = computed(() => [...props.pkg.functions, ...props.pkg.properties])
</script>

<template>
  <div class="api-page">
    <div class="api-eyebrow"><span class="api-kind">package</span></div>
    <h1 :id="titleAnchor(pkg.name)">{{ pkg.name }}</h1>

    <div v-if="pkg.doc?.html" class="api-prose" v-html="pkg.doc.html" />

    <template v-for="group in groups" :key="group.kind">
      <h2 :id="group.kind" tabindex="-1">
        {{ group.label }}
        <a class="header-anchor" :href="`#${group.kind}`" :aria-label="`Permalink to ${group.label}`" />
      </h2>
      <ul class="api-list">
        <li v-for="c in group.items" :key="c.id">
          <a :href="c.url">{{ c.name }}</a>
          <span v-if="c.doc?.summary" class="api-summary">{{ c.doc.summary }}</span>
        </li>
      </ul>
    </template>

    <template v-if="topLevel.length">
      <h2 id="top-level" tabindex="-1">
        Top-level declarations
        <a class="header-anchor" href="#top-level" aria-label="Permalink to Top-level declarations" />
      </h2>
      <ApiMember v-for="m in topLevel" :key="m.id + m.anchor" :member="m" />
    </template>
  </div>
</template>

<style scoped>
.api-eyebrow {
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

h1 {
  margin: 0;
  font-family: var(--vp-font-family-mono);
  font-size: 1.6rem;
  font-weight: 650;
  letter-spacing: -0.02em;
}

h2 {
  margin: 2.5rem 0 0;
  padding-top: 1.5rem;
  border-top: 1px solid var(--vp-c-divider);
  font-size: 1.3rem;
}

.api-list {
  list-style: none;
  margin: 1rem 0 0;
  padding: 0;
}

.api-list li {
  display: grid;
  grid-template-columns: minmax(10rem, 16rem) 1fr;
  gap: 0.25rem 1.5rem;
  padding: 0.55rem 0;
  border-bottom: 1px solid var(--vp-c-divider);
}

.api-list a {
  font-family: var(--vp-font-family-mono);
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
}

.api-summary {
  color: var(--vp-c-text-2);
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .api-list li {
    grid-template-columns: 1fr;
  }
}
</style>
