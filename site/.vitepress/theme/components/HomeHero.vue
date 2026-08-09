<script setup lang="ts">
import { computed, ref } from 'vue'
import CodeLines from './CodeLines.vue'

interface Panel {
  id: string
  label: string
  kind: 'code' | 'terminal'
  file: string
  code?: string
  command?: string
  output?: string
}

const props = defineProps<{
  data: { version: string; install: string; panels: Panel[] } | null
}>()

const active = ref(0)
const copied = ref(false)

const panels = computed(() => props.data?.panels ?? [])
const current = computed(() => panels.value[active.value])

async function copyInstall() {
  if (!props.data) return
  try {
    await navigator.clipboard.writeText(props.data.install)
    copied.value = true
    setTimeout(() => (copied.value = false), 1600)
  } catch {
    // Clipboard access can be refused; the line is selectable, so there is nothing to recover from.
  }
}

/** Left/right arrows move between panels, which is what a tablist is expected to do. */
function onKey(event: KeyboardEvent) {
  const count = panels.value.length
  if (event.key === 'ArrowRight') active.value = (active.value + 1) % count
  else if (event.key === 'ArrowLeft') active.value = (active.value - 1 + count) % count
  else return
  event.preventDefault()
}
</script>

<template>
  <section class="hero">
    <div class="hero-grid" aria-hidden="true" />

    <div class="hero-inner">
      <div class="hero-left">
        <div class="hero-eyebrow">
          <span class="a-eyebrow"><span class="dot" />Kotlin · JVM · Zero dependencies</span>
          <span v-if="data" class="a-eyebrow">v{{ data.version }}</span>
        </div>

        <h1>Typed command-line parsing, without the ceremony.</h1>

        <p class="hero-lead">
          Declare your parameters once and get validated values at startup. No runtime reflection,
          no third-party dependencies.
        </p>

        <!-- target="_self" forces a real navigation: the versioned tree is a separate VitePress
             build, so the router would otherwise intercept these and render its own 404. -->
        <div class="hero-actions">
          <a class="btn btn-primary" href="/docs/latest/learn/" target="_self">
            Get started <span aria-hidden="true">→</span>
          </a>
          <a class="btn btn-ghost" href="/docs/latest/api/" target="_self">API reference</a>
        </div>

        <div v-if="data" class="hero-install">
          <code>{{ data.install }}</code>
          <button type="button" @click="copyInstall">{{ copied ? 'Copied' : 'Copy' }}</button>
        </div>
      </div>

      <div v-if="current" class="hero-right">
        <div class="tabs" role="tablist" aria-label="Ways to declare a command line" @keydown="onKey">
          <button
            v-for="(p, i) in panels"
            :key="p.id"
            role="tab"
            type="button"
            :aria-selected="i === active"
            :tabindex="i === active ? 0 : -1"
            :class="{ on: i === active }"
            @click="active = i"
          >
            {{ p.label }}
          </button>
        </div>

        <div class="window">
          <div class="window-bar">
            <span class="window-title">{{ current.file }}</span>
            <span class="window-tag">{{ current.kind === 'terminal' ? 'generated' : 'source' }}</span>
          </div>

          <!-- Keyed so Vue replaces the node on every change, which replays the entry animation.
               A <Transition> would need a leave phase to finish before the next panel mounts, and
               out-in left the outgoing panel stranded when its transitionend never arrived. -->
          <div class="window-body">
            <div :key="current.id" class="panel">
              <pre v-if="current.kind === 'terminal'" class="term"><span class="prompt">$</span> {{ current.command }}
{{ current.output }}
<span class="prompt">$</span> <span class="caret" aria-hidden="true" /></pre>
              <CodeLines v-else :code="current.code ?? ''" />
            </div>
          </div>
        </div>

        <p class="window-note">
          The same program, declared both ways, and the help screen Argot generates from it.
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  border-bottom: 1px solid var(--a-hairline);
  overflow: hidden;
}

/* The faint 88px lattice behind the hero, drawn rather than imaged so it costs nothing. */
.hero-grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(var(--a-grid) 1px, transparent 1px),
    linear-gradient(90deg, var(--a-grid) 1px, transparent 1px);
  background-size: 88px 88px;
  mask-image: linear-gradient(to bottom, #000 0%, transparent 85%);
  pointer-events: none;
}

.hero-inner {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.05fr);
  gap: 4rem;
  align-items: center;
  max-width: var(--a-max-width);
  margin: 0 auto;
  padding: 88px var(--a-gutter) 96px;
}

.hero-eyebrow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 1.75rem;
  border-bottom: 1px solid var(--a-hairline);
}

.dot {
  display: inline-block;
  width: 5px;
  height: 5px;
  margin-right: 0.75em;
  border-radius: 50%;
  background: var(--a-accent);
  vertical-align: middle;
}

h1 {
  margin: 2rem 0 0;
  font-size: clamp(38px, 4.6vw, 78px);
  line-height: 0.94;
  letter-spacing: -0.045em;
  font-weight: 600;
  color: var(--a-ink);
  text-wrap: balance;
}

.hero-lead {
  max-width: 30rem;
  margin: 1.6rem 0 0;
  font-size: 19px;
  line-height: 1.55;
  color: var(--a-text-body);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 2rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1.4rem;
  border-radius: var(--a-radius-pill);
  font-size: 14px;
  font-weight: 500;
  letter-spacing: -0.01em;
  text-decoration: none;
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;
}

.btn:active {
  transform: translateY(1px);
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

.hero-install {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1.75rem;
  padding: 0.5rem 0.5rem 0.5rem 1rem;
  border: 1px solid var(--a-hairline);
  border-radius: var(--a-radius-pill);
  background: var(--a-card);
  max-width: 100%;
}

.hero-install code {
  font-family: var(--a-font-mono);
  font-size: 12.5px;
  color: var(--a-text-secondary);
  overflow-x: auto;
  white-space: nowrap;
}

.hero-install button {
  flex: none;
  padding: 0.35rem 0.8rem;
  border: 1px solid var(--a-hairline);
  border-radius: var(--a-radius-pill);
  background: transparent;
  font-family: var(--a-font-mono);
  font-size: 10.5px;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--a-text-muted);
  cursor: pointer;
}

.hero-install button:hover {
  border-color: var(--a-accent);
  color: var(--a-accent);
}

.tabs {
  display: inline-flex;
  gap: 2px;
  margin-bottom: 1rem;
  padding: 3px;
  border: 1px solid var(--a-hairline);
  border-radius: var(--a-radius-pill);
  background: var(--a-card);
}

.tabs button {
  padding: 0.4rem 0.95rem;
  border: none;
  border-radius: var(--a-radius-pill);
  background: transparent;
  font-family: var(--a-font-display);
  font-size: 13px;
  font-weight: 500;
  color: var(--a-text-muted);
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}

.tabs button.on {
  background: var(--a-ink);
  color: var(--a-paper);
}

.tabs button:focus-visible {
  outline: 2px solid var(--a-accent);
  outline-offset: 2px;
}

.window {
  border-radius: var(--a-radius-panel);
  background: var(--a-panel-bg);
  box-shadow: var(--a-shadow-terminal);
  overflow: hidden;
}

.window-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.window-title {
  flex: 1;
  font-family: var(--a-font-mono);
  font-size: 11.5px;
  color: var(--a-panel-muted);
}

.window-tag {
  font-family: var(--a-font-mono);
  font-size: 10.5px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--a-panel-accent);
}

/* Fixed rather than min: the three panels are different lengths, and letting the window resize
   shunts the whole page up and down every time someone changes tab. */
.window-body {
  height: 348px;
  padding: 1.1rem 1.25rem;
  overflow-y: auto;
}

.panel {
  animation: panel-in 0.22s ease both;
}

@keyframes panel-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
}

.term {
  margin: 0;
  font-family: var(--a-font-mono);
  font-size: 13px;
  line-height: 1.85;
  color: var(--a-panel-text);
  white-space: pre-wrap;
}

.prompt {
  color: var(--a-panel-prompt);
}

.caret {
  display: inline-block;
  width: 7px;
  height: 15px;
  background: var(--a-panel-text);
  vertical-align: -3px;
  animation: blink 1.1s steps(2, start) infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.window-note {
  max-width: 34rem;
  margin: 1rem 0 0;
  font-size: 13.5px;
  line-height: 1.5;
  color: var(--a-text-faintest);
}

@media (max-width: 960px) {
  .hero-inner {
    grid-template-columns: 1fr;
    gap: 3rem;
    padding: 56px var(--a-gutter) 64px;
  }
}

@media (max-width: 640px) {
  :root {
    --a-gutter: 20px;
  }

  .hero-eyebrow {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.4rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .btn,
  .tabs button {
    transition: none;
  }

  .caret,
  .panel {
    animation: none;
  }
}
</style>
