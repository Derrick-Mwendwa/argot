<script setup lang="ts">
defineProps<{ url?: string; path?: string }>()
</script>

<template>
  <a v-if="url" class="api-source" :href="url" target="_blank" rel="noreferrer">
    {{ path?.split('/').pop() ?? 'source' }}
    <svg width="10" height="10" viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
        stroke-linecap="round"
        stroke-linejoin="round"
        d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6M15 3h6v6M10 14 21 3"
      />
    </svg>
  </a>
</template>

<style scoped>
.api-source {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  margin-top: 0.35rem;
  font-family: var(--vp-font-family-mono);
  font-size: 0.72rem;
  color: var(--vp-c-text-3);
  text-decoration: none;
}

.api-source:hover {
  color: var(--vp-c-brand-1);
}
</style>
