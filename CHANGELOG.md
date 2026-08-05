# Changelog

All notable changes to Argot are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and Argot follows
[Semantic Versioning](https://semver.org).

## Unreleased

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
