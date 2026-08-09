---
title: Documentation versions
---

<script setup>
import { ref, onMounted } from 'vue'

const versions = ref(null)
const failed = ref(false)

onMounted(async () => {
  try {
    const res = await fetch('/versions.json')
    versions.value = await res.json()
  } catch {
    failed.value = true
  }
})
</script>

# Documentation versions

Each release keeps its own documentation, describing that version and no other.

<div v-if="versions">
  <ul>
    <li v-for="v of versions" :key="v.id">
      <a :href="`/docs/${v.id}/learn/`"><strong>{{ v.label ?? v.id }}</strong></a>
      &mdash;
      <a :href="`/docs/${v.id}/learn/`">guides</a> ·
      <a :href="`/docs/${v.id}/api/`">reference</a>
    </li>
  </ul>
</div>
<p v-else-if="failed">Version list unavailable. Try <a href="/docs/latest/learn/">the latest docs</a>.</p>
<p v-else>Loading…</p>

`latest` always points at the newest release. Older releases appear here as they are documented.
