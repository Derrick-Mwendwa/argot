---
title: News
---

<script setup>
import { data as posts } from './posts.data.mts'

const formatted = (d) =>
  new Date(d).toLocaleDateString('en-GB', { year: 'numeric', month: 'long', day: 'numeric' })
</script>

# News

Releases, what is being worked on next, and anything worth saying out loud.

<ul class="news-list">
  <li v-for="post of posts" :key="post.url">
    <a :href="post.url">{{ post.title }}</a>
    <span class="news-date">{{ formatted(post.date) }}</span>
    <span v-if="post.tag" class="news-tag">{{ post.tag }}</span>
  </li>
</ul>

<style scoped>
.news-list { list-style: none; padding: 0; }
.news-list li { padding: 0.75rem 0; border-bottom: 1px solid var(--vp-c-divider); }
.news-list a { font-weight: 600; }
.news-date { display: block; font-size: 0.85em; color: var(--vp-c-text-2); }
.news-tag {
  display: inline-block; margin-top: 0.25rem; padding: 0.1rem 0.5rem;
  font-size: 0.75em; border-radius: 999px;
  background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1);
}
</style>
