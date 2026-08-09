---
layout: page
title: Typed command-line parsing for Kotlin
description: Declare your parameters once and get validated values at startup. No runtime reflection, no third-party dependencies.
---

<script setup>
import { data as hero } from './hero.data.mts'
</script>

<HomeHero :data="hero" />
<HomePromises />
<HomeInstall :version="hero?.version ?? null" />
