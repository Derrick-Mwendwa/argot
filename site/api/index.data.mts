import { packages } from '../.vitepress/api/model.mts'

/** Feeds the reference landing page, so it can never list a package that was not generated. */
export default {
  watch: ['../../build/dokka/html/*/api.json'],
  load() {
    return packages.map((pkg) => ({
      name: pkg.name,
      url: pkg.url,
      summary: pkg.doc?.summary ?? null,
      classlikes: pkg.classlikes.length,
      topLevel: pkg.functions.length + pkg.properties.length,
    }))
  },
}
