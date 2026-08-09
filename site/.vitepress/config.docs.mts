import type { DefaultTheme, UserConfig } from 'vitepress'
import { packages, searchHtmlFor } from './api/model.mts'
import { DOCS_VERSION, REPO, sharedConfig } from './shared.mts'

/**
 * The model's URLs include the base, because components render them into raw `<a href>` where
 * nothing else would add it. Theme config is the opposite: VitePress prepends the base itself, so a
 * link left as-is here resolves to /docs/dev/docs/dev/…
 */
function withoutBase(url: string): string {
  return url.startsWith(base) ? url.slice(base.length - 1) : url
}

/**
 * Sidebar labels are rendered as HTML, so a package name can carry its own break opportunities.
 * Without them a long dotted identifier splits mid-word ("…argot.ann / otations").
 */
function packageLabel(name: string): string {
  return `<span class="a-mono">${name.replace(/\./g, '.<wbr>')}</span>`
}

/** Built from the generated model, so it cannot list a type the reference does not contain. */
function referenceSidebar(): DefaultTheme.SidebarItem[] {
  return [
    { text: 'Overview', link: '/api/' },
    ...packages.map((pkg) => ({
      text: packageLabel(pkg.name),
      collapsed: false,
      items: [
        { text: 'Package summary', link: withoutBase(pkg.url) },
        ...pkg.classlikes.map((cls) => ({
          text: `<span class="a-mono">${cls.name}</span>`,
          link: withoutBase(cls.url),
        })),
      ],
    })),
  ]
}

/**
 * The versioned half: the guided path and the API reference, deployed to /docs/<version>/.
 *
 * The reference is generated from tools/dokka-json's output by the routes under api/, so it is an
 * ordinary part of this build rather than a separate tree copied in beside it.
 */
const base = `/docs/${DOCS_VERSION}/`

export const docsConfig: UserConfig<DefaultTheme.Config> = {
  ...sharedConfig,
  base,
  outDir: `../dist/docs/${DOCS_VERSION}`,
  srcExclude: ['news/**', 'index.md'],
  // The versioned tree needs its own landing page and its own News route: VitePress prepends the
  // base to every themeConfig link, so nothing in this nav can address a path outside /docs/<v>/.
  rewrites: {
    'docs-home.md': 'index.md',
    'docs-news.md': 'news/index.md',
  },
  themeConfig: {
    ...sharedConfig.themeConfig,
    search: {
      provider: 'local',
      options: {
        // Reference pages render a component, so the Markdown they are built from carries no text to
        // index. Without this the whole reference is invisible to search.
        _render(src, env, md) {
          const html = md.render(src, env)
          const reference = searchHtmlFor(env.relativePath ?? '')
          return reference ? `${html}\n${reference}` : html
        },
      },
    },
    nav: [
      { text: 'Learn', link: '/learn/' },
      { text: 'Reference', link: '/api/' },
      { text: 'News', link: '/news/' },
      {
        text: DOCS_VERSION,
        items: [
          { text: 'All versions', link: '/versions', target: '_self' },
          { text: 'Changelog', link: `${REPO}/blob/main/CHANGELOG.md` },
        ],
      },
    ],
    sidebar: {
      '/api/': referenceSidebar(),
      '/learn/': [
        {
          text: 'Tutorial',
          collapsed: false,
          items: [
            { text: 'Introduction', link: '/learn/' },
            { text: '1. Your first CLI', link: '/learn/tutorial/your-first-cli' },
            { text: '2. The same CLI, with annotations', link: '/learn/tutorial/annotation-style' },
          ],
        },
        {
          text: 'How-to guides',
          collapsed: false,
          items: [
            { text: 'Overview', link: '/learn/how-to/' },
            { text: 'Collect a repeated option', link: '/learn/how-to/repeated-options' },
            { text: 'Write a custom converter', link: '/learn/how-to/custom-converter' },
            { text: 'Optional positionals', link: '/learn/how-to/optional-positionals' },
            { text: 'Accept a fixed set of values', link: '/learn/how-to/enum-values' },
            { text: 'Choose a style', link: '/learn/how-to/choosing-a-style' },
          ],
        },
        {
          text: 'Explanation',
          collapsed: false,
          items: [
            { text: 'Overview', link: '/learn/explanation/' },
            { text: 'Two styles, one engine', link: '/learn/explanation/two-styles' },
            { text: 'No runtime reflection', link: '/learn/explanation/no-reflection' },
            { text: 'How optionality is inferred', link: '/learn/explanation/optionality' },
            { text: 'What breaks a release', link: '/learn/explanation/breaking-changes' },
          ],
        },
      ],
    },
    editLink: {
      pattern: `${REPO}/edit/main/site/:path`,
      text: 'Edit this page on GitHub',
    },
  },
}
