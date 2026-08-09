import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import '@fontsource-variable/space-grotesk'
import '@fontsource-variable/jetbrains-mono'
import ApiClasslike from './components/ApiClasslike.vue'
import ApiPackage from './components/ApiPackage.vue'
import HomeHero from './components/HomeHero.vue'
import HomeInstall from './components/HomeInstall.vue'
import HomePromises from './components/HomePromises.vue'
import './tokens.css'
import './custom.css'

/**
 * Shared by both builds, so the reference and the guides are the same product rather than two that
 * were made to resemble each other.
 */
export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ApiClasslike', ApiClasslike)
    app.component('ApiPackage', ApiPackage)
    app.component('HomeHero', HomeHero)
    app.component('HomePromises', HomePromises)
    app.component('HomeInstall', HomeInstall)
  },
} satisfies Theme
