<script setup lang="ts">
import { computed } from 'vue'
import CodeLines from './CodeLines.vue'

const props = defineProps<{ version: string | null }>()

/** Written here rather than generated: this is build configuration, not Argot's own output. */
const snippet = computed(() => {
  const v = props.version ?? '0.1.2'
  return `dependencies {
    implementation("org.draftcode:argot-core:${v}")
}

// Only for the annotation style:
plugins {
    id("com.google.devtools.ksp") version "2.3.11"
}

dependencies {
    implementation("org.draftcode:argot-annotations:${v}")
    ksp("org.draftcode:argot-processor:${v}")
}`
})
</script>

<template>
  <section class="install">
    <div class="inner">
      <div class="lede">
        <p class="a-eyebrow">§ 02 — Install</p>
        <h2>One dependency to start.</h2>
        <p class="body">
          <code>argot-core</code> is the whole delegate API — nothing else to add, nothing to
          configure. The annotation style needs two more artifacts and the KSP plugin.
        </p>
        <div class="actions">
          <a class="btn btn-primary" href="/docs/latest/learn/" target="_self">Read the guide</a>
          <a class="btn btn-ghost" href="https://github.com/Derrick-Mwendwa/argot">GitHub</a>
        </div>
      </div>

      <div class="panel">
        <div class="panel-bar">
          <span class="panel-title">build.gradle.kts</span>
        </div>
        <div class="panel-body"><CodeLines :code="snippet" /></div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.inner {
  display: grid;
  grid-template-columns: minmax(0, 0.85fr) minmax(0, 1.15fr);
  gap: 4rem;
  align-items: center;
  max-width: var(--a-max-width);
  margin: 0 auto;
  padding: 96px var(--a-gutter);
}

h2 {
  margin: 1.1rem 0 0;
  font-size: 34px;
  line-height: 1.04;
  letter-spacing: -0.035em;
  font-weight: 600;
  text-wrap: balance;
}

.body {
  margin: 1.2rem 0 0;
  font-size: 16.5px;
  line-height: 1.6;
  color: var(--a-text-body);
}

.body code {
  padding: 0.15em 0.4em;
  border-radius: var(--a-radius-chip-sm);
  background: var(--a-chip);
  font-family: var(--a-font-mono);
  font-size: 0.86em;
  color: var(--a-text-secondary);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 2rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  padding: 0.7rem 1.4rem;
  border-radius: var(--a-radius-pill);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.btn-primary {
  background: var(--a-ink);
  color: var(--a-paper);
}

.btn-primary:hover {
  background: var(--a-accent);
}

.btn-ghost {
  border: 1px solid var(--a-hairline-strong);
  color: var(--a-ink);
}

.btn-ghost:hover {
  border-color: var(--a-accent);
  color: var(--a-accent);
}

.panel {
  border-radius: var(--a-radius-panel);
  background: var(--a-panel-bg);
  overflow: hidden;
}

.panel-bar {
  padding: 0.7rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.panel-title {
  font-family: var(--a-font-mono);
  font-size: 11.5px;
  color: var(--a-panel-muted);
}

.panel-body {
  padding: 1.1rem 1.25rem;
  overflow-x: auto;
}

@media (max-width: 960px) {
  .inner {
    grid-template-columns: 1fr;
    gap: 2.5rem;
    padding: 64px var(--a-gutter);
  }
}
</style>
