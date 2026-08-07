---
layout: home

hero:
  name: Argot
  text: Typed command-line parsing for Kotlin
  tagline: Declare your parameters once, get validated values at startup. No runtime reflection, no third-party dependencies.
  actions:
    - theme: brand
      text: Get started
      link: /docs/latest/learn/
    - theme: alt
      text: API reference
      link: /docs/latest/api/
    - theme: alt
      text: GitHub
      link: https://github.com/Derrick-Mwendwa/argot

features:
  - title: Two styles, one engine
    details: Plain Kotlin delegates, or annotate a data class and let a KSP processor write the parser. Behaviour and help output are identical either way.
  - title: Nothing at runtime
    details: argot-core and argot-annotations depend only on the Kotlin standard library. KSP and KotlinPoet never reach your runtime classpath.
  - title: Errors at compile time
    details: Duplicate names, misplaced positionals and unsupported types are reported while you build, not when a user runs your program.
---
