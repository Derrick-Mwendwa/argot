import { existsSync, readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

/**
 * The landing page hero's panels, produced by `./gradlew :docs-samples:heroData`.
 *
 * Null when that has not run, which is normal in a prose-only dev session: the hero renders without
 * the panel rather than inventing declarations the library never produced.
 */
const here = dirname(fileURLToPath(import.meta.url))
const file = resolve(here, '../docs-samples/build/hero/hero.json')

export interface HeroPanel {
  id: string
  label: string
  kind: 'code' | 'terminal'
  file: string
  code?: string
  command?: string
  output?: string
}

export interface HeroData {
  version: string
  install: string
  panels: HeroPanel[]
}

export default {
  watch: [file],
  load(): HeroData | null {
    if (!existsSync(file)) {
      if (process.env.ARGOT_API_OPTIONAL !== '1') {
        throw new Error(
          `[argot] No ${file}. Run ./gradlew :docs-samples:heroData to build the landing page.`,
        )
      }
      console.warn(`[argot] No ${file}; the landing page hero panel will be hidden this session.`)
      return null
    }
    return JSON.parse(readFileSync(file, 'utf-8'))
  },
}
