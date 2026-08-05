# Contributing to Argot

## Building

```sh
./gradlew build      # compiles every module and runs all tests
```

JDK 17 is the toolchain. Gradle provisions it automatically if you don't have it.

## Branching

`main` is protected: it always builds, always passes tests, and is always in a releasable state.
Nothing is pushed to it directly.

All work happens on a short-lived branch off `main`, merged through a pull request once CI is green:

```sh
git switch -c fix/generated-code-spacing
# ... work, commit ...
git push -u origin fix/generated-code-spacing
gh pr create
```

Branch names are prefixed by intent: `feat/`, `fix/`, `docs/`, or `chore/`. Delete the branch after
merging — the only long-lived branch is `main`.

The one exception is a `release/0.x.x` branch, cut from an old tag when a previous release line needs
a patch. See [RELEASING.md](RELEASING.md).

## Commits

Short, imperative subject lines, the way commits normally read on GitHub:

```
Trim internal comments in the KSP processor
Add a blank line between generated code blocks
Fix the repository URL in the published POM
```

No `feat:`/`fix:` prefixes, no long bodies, no trailers. If a change genuinely needs explanation,
put it in the pull request description where it stays readable.

## Code conventions

- **`argot-core` and `argot-annotations` take no third-party runtime dependencies.** Kotlin stdlib
  only. This is the library's main promise to its users.
- **`argot-processor` is compile-time only.** KSP and KotlinPoet must never reach a consumer's
  runtime classpath.
- **No runtime reflection**, anywhere.
- Explicit API mode is on for the published modules. Public declarations need explicit visibility.
- KDoc on public API is written for someone *using* Argot, not someone maintaining it. Skip it when
  the signature already says everything.
- Internal code carries a comment only when the comment says something the code cannot.

## Public API changes

`./gradlew build` runs `checkKotlinAbi`, which compares each published module against the ABI dump
committed in its `api/` directory. If you change the public API on purpose, regenerate and commit the
dump in the same pull request:

```sh
./gradlew updateKotlinAbi
```

If you did not mean to change it, the failure is telling you something: an accidentally `public`
declaration, or a signature change that would break code compiled against the previous release. The
Versioning section of [RELEASING.md](RELEASING.md) explains why that matters even for declarations
nobody types by hand.

## Tests

Every behavioural change needs a test. The suites are:

- `argot-core` — parsing semantics, spec validation, and help rendering.
- `argot-processor` — real compilations through the KSP2 harness, covering both generated output and
  the compile errors the processor is expected to report.
- `sample` — end-to-end checks of both consumer styles.
