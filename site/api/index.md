---
title: API reference
description: Every public declaration in Argot.
---

<script setup>
import { data as packages } from './index.data.mts'
</script>

# API reference

Every public declaration in `argot-core` and `argot-annotations`. `argot-processor` is not listed:
its only public symbol is the KSP entry point, which nobody calls by hand.

<ul class="ref-packages">
  <li v-for="pkg of packages" :key="pkg.name">
    <a :href="pkg.url">{{ pkg.name }}</a>
    <p v-if="pkg.summary">{{ pkg.summary }}</p>
    <p class="ref-count">
      {{ pkg.classlikes }} types<template v-if="pkg.topLevel"> · {{ pkg.topLevel }} top-level declarations</template>
    </p>
  </li>
</ul>

<p v-if="!packages.length" class="ref-empty">
  The reference is unavailable for this version.
</p>

::: tip Looking for how to use it?
The reference answers "what is the exact signature". For "how do I do X", start with the
[guides](/learn/).
:::

<style scoped>
.ref-packages {
  list-style: none;
  padding: 0;
  margin: 2rem 0;
}

.ref-packages li {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding: 1.1rem 0;
  border-bottom: 1px solid var(--a-hairline);
}

.ref-packages li:first-child {
  border-top: 1px solid var(--a-hairline);
}

.ref-packages a {
  font-family: var(--a-font-mono);
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: -0.02em;
  text-decoration: none;
}

.ref-packages a:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.ref-packages p {
  margin: 0;
  color: var(--a-text-body);
}

.ref-count {
  font-family: var(--a-font-mono);
  font-size: 11.5px;
  letter-spacing: 0.06em;
  color: var(--a-text-faintest);
}

.ref-empty {
  color: var(--vp-c-text-2);
}
</style>
