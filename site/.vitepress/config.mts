import { defineConfig } from 'vitepress'
import { docsConfig } from './config.docs.mts'
import { rootConfig } from './config.root.mts'

/**
 * VitePress's CLI takes no `--config` flag — it only ever loads `.vitepress/config.*` — so the two
 * builds are selected here by environment variable instead:
 *
 *   ARGOT_BUILD=root  ->  /            landing and news, unversioned
 *   ARGOT_BUILD=docs  ->  /docs/<v>/   the guided path, one tree per release
 *
 * Passing an unknown value is a build error rather than a silent fallback: getting this wrong would
 * publish one half of the site over the other.
 */
const target = process.env.ARGOT_BUILD ?? 'docs'

if (target !== 'root' && target !== 'docs') {
  throw new Error(`ARGOT_BUILD must be "root" or "docs", got "${target}"`)
}

export default defineConfig(target === 'root' ? rootConfig : docsConfig)
