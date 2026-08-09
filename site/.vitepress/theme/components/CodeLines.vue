<script setup lang="ts">
/**
 * Kotlin highlighting for strings we render ourselves.
 *
 * Shiki handles Markdown code fences, but these panels come from build-generated JSON rather than
 * from a fence, so they are tokenised here. Tokens are rendered as elements rather than as HTML —
 * nothing is interpolated into markup, so a sample containing angle brackets cannot break out.
 */
import { computed } from 'vue'

const props = defineProps<{ code: string }>()

const TOKENS =
  /(\/\/[^\n]*)|("(?:[^"\\]|\\.)*")|(@[A-Za-z]\w*)|\b(class|val|var|by|import|fun|object|data|true|false|package)\b|\b(\d+)\b/g

interface Token {
  text: string
  kind: string
}

function tokenize(line: string): Token[] {
  const out: Token[] = []
  let last = 0
  for (const m of line.matchAll(TOKENS)) {
    const at = m.index ?? 0
    if (at > last) out.push({ text: line.slice(last, at), kind: '' })
    const kind = m[1]
      ? 'comment'
      : m[2]
        ? 'string'
        : m[3]
          ? 'annotation'
          : m[4]
            ? 'keyword'
            : 'number'
    out.push({ text: m[0], kind })
    last = at + m[0].length
  }
  if (last < line.length) out.push({ text: line.slice(last), kind: '' })
  return out
}

// Computed, not a plain const: the hero reuses this component across carousel panels, so a value
// captured once at setup would keep showing whichever panel rendered first.
const lines = computed(() => props.code.split('\n'))
</script>

<template>
  <pre class="code"><code><span v-for="(line, i) in lines" :key="i" class="line"><span
    v-for="(t, j) in tokenize(line)" :key="j" :class="t.kind">{{ t.text }}</span>{{ '\n' }}</span></code></pre>
</template>

<style scoped>
.code {
  margin: 0;
  padding: 0;
  font-family: var(--a-font-mono);
  font-size: 13px;
  line-height: 1.85;
  color: var(--a-panel-text);
}

/* Wrapped rather than scrolled: a declaration whose tail sits off-panel is the one thing these
   panels exist to show, and nobody scrolls a hero horizontally to find it. */
.line {
  display: block;
  padding-left: 1.6em;
  text-indent: -1.6em;
  white-space: pre-wrap;
  overflow-wrap: break-word;
}

.keyword {
  color: var(--a-panel-keyword);
}

.string {
  color: var(--a-panel-string);
}

.annotation {
  color: var(--a-panel-annotation);
}

.comment {
  color: var(--a-panel-comment);
}

.number {
  color: var(--a-panel-string);
}
</style>
