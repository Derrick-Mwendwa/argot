<script setup lang="ts">
import { computed } from 'vue'
import type { Member } from '../../api/model.mts'
import ApiTokens from './ApiTokens.vue'
import ApiSource from './ApiSource.vue'

const props = defineProps<{ member: Member }>()

/**
 * Everything before the declaration's name, assembled here rather than as separate template spans.
 *
 * Vue's compiler strips trailing whitespace from static text that sits on its own line, so the
 * spaces after `open` and `fun` were being removed at build time and the signature rendered as
 * `open overrideval`. Spacing produced by an expression survives.
 */
const prefix = computed(() => {
  const m = props.member
  const parts = [...(m.modifiers ?? [])]
  if (m.kind === 'function') parts.push('fun')
  else if (m.kind === 'property') parts.push(m.mutable ? 'var' : 'val')
  if (m.typeParameters?.length) {
    parts.push(`<${m.typeParameters.map((t) => t.name).join(', ')}>`)
  }
  return parts.length ? `${parts.join(' ')} ` : ''
})
</script>

<template>
  <div class="api-member">
    <h3 :id="member.anchor" tabindex="-1">
      <span class="api-member-name">{{ member.name }}</span>
      <a class="header-anchor" :href="`#${member.anchor}`" :aria-label="`Permalink to ${member.name}`" />
    </h3>

    <div class="api-decl">
      <span v-if="prefix" class="api-kw">{{ prefix }}</span>
      <template v-if="member.receiver">
        <ApiTokens :tokens="member.receiver" /><span>.</span>
      </template>
      <span class="api-decl-name">{{ member.name }}</span>
      <template v-if="member.kind !== 'property'">
        <span>(</span>
        <span v-if="member.parameters?.length" class="api-params">
          <span v-for="(p, i) in member.parameters" :key="p.name" class="api-param">
            <span class="api-param-name">{{ p.name }}</span><span>: </span>
            <ApiTokens :tokens="p.type" /><span v-if="i < member.parameters.length - 1">, </span>
          </span>
        </span>
        <span>)</span>
      </template>
      <template v-if="member.returnType || member.type">
        <span>: </span><ApiTokens :tokens="member.returnType ?? member.type" />
      </template>
    </div>

    <div v-if="member.doc?.deprecated" class="api-deprecated">
      <strong>Deprecated.</strong> <span v-html="member.doc.deprecated" />
    </div>

    <div v-if="member.doc?.html" class="api-prose" v-html="member.doc.html" />

    <dl v-if="member.parameters?.some((p) => p.doc)" class="api-params-doc">
      <template v-for="p in member.parameters" :key="p.name">
        <template v-if="p.doc">
          <dt>{{ p.name }}</dt>
          <dd v-html="p.doc" />
        </template>
      </template>
    </dl>

    <div v-if="member.doc?.returns" class="api-note">
      <span class="api-note-label">Returns</span>
      <span v-html="member.doc.returns" />
    </div>

    <div v-for="t in member.doc?.throws ?? []" :key="t.type" class="api-note api-note-throws">
      <span class="api-note-label">Throws</span>
      <a v-if="t.url" :href="t.url" class="api-throws-type">{{ t.type }}</a>
      <code v-else class="api-throws-type">{{ t.type }}</code>
      <span v-html="t.html" />
    </div>

    <div v-if="member.doc?.since" class="api-since">Since {{ member.doc.since }}</div>

    <ApiSource :url="member.sourceUrl" :path="member.source" />
  </div>
</template>

<style scoped>
.api-member {
  padding: 1.25rem 0 0.25rem;
  border-top: 1px solid var(--vp-c-divider);
}

.api-member h3 {
  margin: 0;
  font-size: 1.05rem;
  letter-spacing: -0.01em;
}

.api-member-name {
  font-family: var(--vp-font-family-mono);
}

.api-decl {
  margin: 0.5rem 0 0.75rem;
  padding: 0.6rem 0.8rem;
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
  font-family: var(--vp-font-family-mono);
  font-size: 0.8125rem;
  line-height: 1.7;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.api-kw {
  color: var(--vp-c-text-3);
}

.api-decl-name {
  font-weight: 600;
  color: var(--vp-c-text-1);
}

.api-param-name {
  color: var(--vp-c-text-2);
}

.api-prose :deep(p) {
  margin: 0.5rem 0;
}

.api-prose :deep(p:first-child) {
  margin-top: 0;
}

.api-params-doc {
  display: grid;
  grid-template-columns: minmax(4rem, auto) 1fr;
  gap: 0.15rem 1rem;
  margin: 0.75rem 0;
  padding-left: 0;
  font-size: 0.9rem;
}

.api-params-doc dt {
  font-family: var(--vp-font-family-mono);
  font-size: 0.85em;
  color: var(--vp-c-text-2);
}

.api-params-doc dd {
  margin: 0;
}

.api-params-doc :deep(p) {
  margin: 0;
}

.api-note {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.5rem;
  margin: 0.4rem 0;
  font-size: 0.9rem;
}

.api-note-label {
  flex: none;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--vp-c-text-3);
}

.api-note :deep(p) {
  margin: 0;
}

.api-throws-type {
  font-family: var(--vp-font-family-mono);
  font-size: 0.85em;
  color: var(--vp-c-brand-1);
}

.api-deprecated {
  margin: 0.5rem 0;
  padding: 0.6rem 0.9rem;
  border: 1px solid var(--a-hairline);
  border-radius: var(--a-radius-chip);
  background: var(--vp-c-bg-soft);
  font-size: 0.9rem;
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

.api-since {
  margin: 0.4rem 0;
  font-size: 0.8rem;
  color: var(--vp-c-text-3);
}
</style>
