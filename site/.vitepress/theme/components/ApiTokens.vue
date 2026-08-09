<script setup lang="ts">
import type { TypeToken } from '../../api/model.mts'

defineProps<{ tokens?: TypeToken[] }>()
</script>

<template>
  <span class="api-type">
    <template v-for="(t, i) in tokens ?? []" :key="i">
      <a v-if="t.url" :href="t.url" class="api-type-ref">{{ t.text }}</a>
      <abbr v-else-if="t.external" :title="t.external">{{ t.text }}</abbr>
      <span v-else>{{ t.text }}</span>
    </template>
  </span>
</template>

<style scoped>
.api-type {
  font-family: var(--vp-font-family-mono);
  font-size: 0.875em;
}

.api-type-ref {
  color: var(--vp-c-brand-1);
  text-decoration: none;
}

.api-type-ref:hover {
  text-decoration: underline;
}

/* A type we do not publish. Marked as a definition rather than a link so the fully qualified name is
   available without implying there is a page to visit. */
abbr {
  text-decoration: none;
  border-bottom: 1px dotted var(--vp-c-divider);
  cursor: help;
}
</style>
