import type { DefaultTheme, UserConfig } from 'vitepress'
import { argotDark, argotLight } from './code-theme.mts'

/**
 * Settings both builds share.
 *
 * The site is produced by two VitePress builds writing into one GitHub Pages branch:
 *
 *   config.root.mts  ->  /            landing and news, unversioned
 *   config.docs.mts  ->  /docs/<v>/   the guided path, one tree per release
 *
 * News is deliberately outside the versioned tree. If it were inside, someone reading the 0.1.2
 * docs a year from now would be shown announcements frozen at that release.
 */

export const SITE_TITLE = 'Argot'
export const SITE_DESCRIPTION =
  'A small, zero-dependency Kotlin library for parsing command-line arguments.'

export const REPO = 'https://github.com/Derrick-Mwendwa/argot'

/** Set by the deploy workflow: a release like "0.1.2", or "dev" for the tip of main. */
export const DOCS_VERSION = process.env.ARGOT_DOCS_VERSION ?? 'dev'

export const sharedConfig: UserConfig<DefaultTheme.Config> = {
  lang: 'en-GB',
  title: SITE_TITLE,
  description: SITE_DESCRIPTION,
  cleanUrls: true,
  lastUpdated: true,
  markdown: { theme: { light: argotLight, dark: argotDark } },
  head: [
    ['link', { rel: 'icon', href: '/favicon.svg', type: 'image/svg+xml' }],
    ['meta', { name: 'theme-color', content: '#5B4BEA' }],
  ],
  themeConfig: {
    logo: '/logo.svg',
    search: { provider: 'local' },
    socialLinks: [{ icon: 'github', link: REPO }],
    footer: {
      message: 'Released under the Apache License 2.0.',
      copyright: `Copyright © ${new Date().getFullYear()} The Argot Authors`,
    },
  },
}
