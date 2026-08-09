import { mkdirSync, rmSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { packages } from '../.vitepress/api/model.mts'

/**
 * Writes one Markdown page per package and per classlike under `site/api/`.
 *
 * These were VitePress dynamic routes, which cost the reference two things: 1.6.4's route pattern
 * does not match rest parameters, and — the reason for this script — its local search only indexes
 * Markdown that exists on disk, so the entire reference was missing from search. Real files are
 * indexed like any other page.
 *
 * Each page is a component invocation plus a sibling JSON file holding that symbol's slice of the
 * model, so a page ships only its own data.
 */

const here = dirname(fileURLToPath(import.meta.url))
const apiDir = resolve(here, '../api')

function page(title: string, description: string, component: string, dataFile: string): string {
  return `---
title: ${yaml(title)}
description: ${yaml(description)}
outline: [2, 3]
---

<script setup>
import node from './${dataFile}'
</script>

<${component} :${component === 'ApiPackage' ? 'pkg' : 'cls'}="node" />
`
}

/** Frontmatter values are quoted rather than escaped by hand: KDoc summaries contain colons. */
function yaml(value: string): string {
  return JSON.stringify(value)
}

let written = 0

for (const pkg of packages) {
  const dir = join(apiDir, pkg.name)
  // Removed wholesale first: a type deleted from the library must not leave a page behind.
  rmSync(dir, { recursive: true, force: true })
  mkdirSync(dir, { recursive: true })

  writeFileSync(join(dir, 'index.data.json'), JSON.stringify(pkg))
  writeFileSync(
    join(dir, 'index.md'),
    page(pkg.name, pkg.doc?.summary ?? `Declarations in ${pkg.name}.`, 'ApiPackage', 'index.data.json'),
  )
  written++

  for (const cls of pkg.classlikes) {
    writeFileSync(join(dir, `${cls.name}.data.json`), JSON.stringify(cls))
    writeFileSync(
      join(dir, `${cls.name}.md`),
      page(
        cls.name,
        cls.doc?.summary ?? `${cls.kind} ${cls.name} in ${pkg.name}.`,
        'ApiClasslike',
        `${cls.name}.data.json`,
      ),
    )
    written++
  }
}

console.log(`[argot] generated ${written} reference pages under site/api/`)
