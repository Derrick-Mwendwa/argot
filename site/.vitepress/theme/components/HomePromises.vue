<script setup lang="ts">
const promises = [
  {
    title: 'Two styles, one engine',
    body: 'Plain Kotlin delegates, or annotate a data class and let a KSP processor write the parser. Both build the same specification and run the same engine, so behaviour and help output are identical.',
    href: '/docs/latest/learn/explanation/two-styles',
  },
  {
    title: 'Nothing at runtime',
    body: 'argot-core and argot-annotations depend only on the Kotlin standard library. KSP and KotlinPoet never reach your runtime classpath, and there is no classpath scan at startup.',
    href: '/docs/latest/learn/explanation/no-reflection',
  },
  {
    title: 'Errors at compile time',
    body: 'Duplicate names, misplaced positionals and unsupported types are reported while you build, not when a user runs your program.',
    href: '/docs/latest/learn/explanation/optionality',
  },
]
</script>

<template>
  <section class="promises">
    <div class="inner">
      <div class="lede">
        <p class="a-eyebrow">§ 01 — Why</p>
        <h2>Three promises, kept by the compiler.</h2>
      </div>

      <ol class="rows">
        <li v-for="(p, i) in promises" :key="p.title">
          <span class="num">{{ String(i + 1).padStart(2, '0') }}</span>
          <h3><a :href="p.href" target="_self">{{ p.title }}</a></h3>
          <p>{{ p.body }}</p>
        </li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.promises {
  border-bottom: 1px solid var(--a-hairline);
}

.inner {
  display: grid;
  grid-template-columns: minmax(0, 0.62fr) minmax(0, 1.38fr);
  gap: 3rem;
  max-width: var(--a-max-width);
  margin: 0 auto;
  padding: 96px var(--a-gutter);
}

.lede {
  position: sticky;
  top: calc(var(--a-header-h) + 40px);
  align-self: start;
}

.lede h2 {
  margin: 1.1rem 0 0;
  font-size: 32px;
  line-height: 1.1;
  letter-spacing: -0.035em;
  font-weight: 600;
  text-wrap: balance;
}

.rows {
  list-style: none;
  margin: 0;
  padding: 0;
  border-top: 1px solid var(--a-hairline);
}

.rows li {
  display: grid;
  grid-template-columns: 3.5rem minmax(0, 1fr) minmax(0, 1.15fr);
  gap: 0 2.5rem;
  padding: 2.25rem 1rem 2.25rem 0;
  border-bottom: 1px solid var(--a-hairline);
  transition: background 0.2s ease;
}

.rows li:hover {
  background: var(--a-row-hover);
}

.num {
  font-family: var(--a-font-mono);
  font-size: 11.5px;
  letter-spacing: 0.16em;
  color: var(--a-text-faintest);
  padding-top: 0.4rem;
}

h3 {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  letter-spacing: -0.02em;
  font-weight: 600;
  text-wrap: balance;
}

h3 a {
  color: inherit;
  text-decoration: none;
}

h3 a:hover {
  color: var(--a-accent);
}

p {
  margin: 0;
  font-size: 15.5px;
  line-height: 1.6;
  color: var(--a-text-muted);
}

@media (max-width: 1000px) {
  .inner {
    grid-template-columns: 1fr;
    gap: 2.5rem;
    padding: 64px var(--a-gutter);
  }

  .lede {
    position: static;
  }

  .rows li {
    grid-template-columns: 3rem minmax(0, 1fr);
    gap: 0.5rem 1.5rem;
  }

  .rows li p {
    grid-column: 2;
  }
}
</style>
