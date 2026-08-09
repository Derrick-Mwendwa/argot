import type { DefaultTheme, UserConfig } from 'vitepress'
import { REPO, sharedConfig } from './shared.mts'

/** The unversioned part of the site: the landing page and news. Deployed to the domain root. */
export const rootConfig: UserConfig<DefaultTheme.Config> = {
  ...sharedConfig,
  base: '/',
  outDir: '../dist/root',
  srcExclude: ['learn/**', 'api/**', 'docs-home.md', 'docs-news.md'],
  themeConfig: {
    ...sharedConfig.themeConfig,
    nav: [
      { text: 'Docs', link: '/docs/latest/learn/', target: '_self' },
      { text: 'Reference', link: '/docs/latest/api/', target: '_self' },
      { text: 'News', link: '/news/' },
      { text: 'Releases', link: `${REPO}/releases` },
    ],
    sidebar: {
      '/news/': [{ text: 'News', items: [{ text: 'All posts', link: '/news/' }] }],
    },
  },
}
