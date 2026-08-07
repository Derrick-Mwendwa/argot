# Changelog

All notable changes to Argot are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and Argot follows
[Semantic Versioning](https://semver.org).

## Unreleased

## 0.1.2 — 2026-08-06

No API or behaviour changes.

### Changed

- Generated parsers place each specification argument on its own line, keeping generated code inside
  100 columns instead of running to roughly 130.

### Internal

- Updated KSP to 2.3.11, the Gradle wrapper to 9.6.1, the publishing plugin to 0.37.0, and JUnit to
  6.1.2. Kotlin stays on 2.3.20: no KSP release targets Kotlin 2.4.x yet, and the two must match.
- The public ABI of every published module is recorded under `api/` and checked by `./gradlew build`,
  so an unintended API change now fails CI instead of reaching a release. See the Versioning section
  of `RELEASING.md` for why this matters to code nobody types by hand.
- Weekly Dependabot updates for GitHub Actions and Gradle dependencies.
- Added issue and pull request templates, a security policy, and `.editorconfig`.
- Removed `gradle.properties.template`; `RELEASING.md` is now the single description of the
  publishing credentials.

## 0.1.1 — 2026-08-05

### Fixed

- `@Option`, `@Flag`, and `@Argument` no longer make the Kotlin compiler warn that "this annotation
  is currently applied to the value parameter only, but in the future it will also be applied to
  property" ([KT-73255]). They now declare a single `VALUE_PARAMETER` target, so a plain
  `@Option(...)` on a constructor parameter is unambiguous — no `@param:` prefix and no
  `-Xannotation-default-target` compiler flag needed.
- The published POM pointed at a repository URL that does not exist. It now points at
  <https://github.com/Derrick-Mwendwa/argot>.

### Changed

- Generated parsers are formatted for reading: four-space indentation matching Kotlin's official
  style, and blank lines separating the specification, the parse call, and the constructor call.
- Public API documentation rewritten to address someone using Argot rather than someone maintaining
  it, and trimmed where a signature already said everything.
- Internal implementation commentary removed.

Parsing behaviour is unchanged. The one API-surface change is the narrowed annotation target, which
cannot break code that compiled against 0.1.0: `@param:Option(...)` still works, and `@Option` on a
non-constructor property was never read by the processor.

[KT-73255]: https://youtrack.jetbrains.com/issue/KT-73255

## 0.1.0 — 2026-06-08

Initial release.

- `argot-core` — the parsing engine and the delegate-style API, with no third-party runtime
  dependencies.
- `argot-annotations` — `@Command`, `@Option`, `@Flag`, and `@Argument`, with `SOURCE` retention.
- `argot-processor` — a KSP2 processor that generates a typed `parse<ClassName>` function at compile
  time, with no runtime reflection.
- Long and short options, attached and separated values, combined flag clusters, `--` termination,
  repeatable options, trailing positional lists, and aligned `--help` rendering.
