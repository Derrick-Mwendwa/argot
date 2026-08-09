---
title: News
description: News lives outside the versioned documentation.
layout: page
head:
  - - meta
    - http-equiv: refresh
      content: 0; url=/news/
---

<script setup>
import { onMounted } from 'vue'

// News is deliberately outside the versioned tree, so this route exists only to hand off to it.
// The nav needs the link to resolve inside /docs/<version>/ — VitePress prepends the base to every
// themeConfig link, so it cannot point at a root path directly.
onMounted(() => window.location.replace('/news/'))
</script>

<div class="redirect">
  <p>News lives outside the versioned documentation.</p>
  <p><a href="/news/">Continue to the news index</a></p>
</div>

<style scoped>
.redirect {
  max-width: 32rem;
  margin: 6rem auto;
  padding: 0 1.5rem;
  text-align: center;
}
</style>
