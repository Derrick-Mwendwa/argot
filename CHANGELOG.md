# Changelog

All notable changes to Argot are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and Argot follows
[Semantic Versioning](https://semver.org).

## Unreleased

### Fixed

- `ParsedValues.flag` and `ParsedValues.list` no longer answer a name that was never declared.
  Both used to swallow the mistake, so a typo produced a silently wrong parse rather than an error;
  `value` has always rejected it. They also reject a name of the wrong kind, which the previous
  `as?` cast hid.

  ```kotlin
  // before — no such parameter, no complaint
  parsed.flag("--verbsoe")        // false
  parsed.list<String>("--tags")   // []
  parsed.flag("--count")          // false, for an Int option

  // after
  parsed.flag("--verbsoe")        // IllegalArgumentException: no parameter named '--verbsoe'
  parsed.flag("--count")          // IllegalArgumentException: '--count' is not a flag
  ```

  A declared parameter that was simply not supplied is unaffected: flags still answer their default
  and `multiple` parameters still answer an empty list.

- A `Converter` that throws `ArgotConversionException` now has its message shown to the user. It was
  discarded, so the explanation a converter author wrote was unreachable — including the built-in
  converters', which is why a rejected enum value never listed the valid ones.

  ```console
  # before
  error: invalid value 'loud' for --level (expected Level)

  # after
  error: invalid value for --level: 'loud' is not a valid Level (expected one of DEBUG, INFO, WARN, ERROR)
  ```

  Any other exception out of a converter keeps the previous wording, since its message is written
  for a stack trace rather than a terminal. Either way the original exception is now attached as the
  `cause`.

### Changed

- `ArgotParseException` accepts a `cause`, and `ArgotParseException.InvalidValue` carries the
  converter's explanation as `detail`. Both are new parameters with defaults, so existing calls still
  compile — but the previous constructors no longer exist as binary signatures, so code compiled
  against 0.1.x must be recompiled. Neither type is part of the generated-code contract, so a
  processor and core from different versions are unaffected.

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
