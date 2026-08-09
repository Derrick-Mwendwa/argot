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

## The documentation site

[argot.draftcode.org](https://argot.draftcode.org) is built from `site/` and deployed by
`.github/workflows/docs.yml`. To work on it:

```sh
cd site
npm install
npm run dev        # the guides
npm run dev:root   # the landing page and news
```

Two rules keep the site honest:

- **Code blocks in the guides are never typed by hand.** They are regions of real files in
  `docs-samples/`, pulled in with VitePress's `<<<` syntax. Add a `// #region name` marker around the
  code you want to show and reference it from the Markdown. If a snippet appears on the site, a
  compiler has checked it.
- **`docs-samples` is verified against the published artifact at release time**, not just the working
  tree, so a guide cannot document behaviour that only exists on `main`:

  ```sh
  ./gradlew :docs-samples:build                      # against the local project
  ./gradlew :docs-samples:build -PargotVersion=0.1.2  # against Maven Central
  ```

Because the reference and the landing page are generated, the site needs a Gradle run before it will
build:

```sh
./gradlew dokkaGenerate :docs-samples:mirrorData
```

`dokkaGenerate` writes `build/dokka/html/*/api.json` and `mirrorData` writes the landing page's
declaration-and-help pairing. `npm run dev` tolerates both being absent — the reference is empty and
the Mirror is hidden — but a production build fails rather than shipping a page with a hole in it.

### The API reference

The reference is generated from KDoc, so it cannot drift — which also means KDoc edits are
user-facing documentation edits. What renders it is `tools/dokka-json`, a Dokka plugin that replaces
Dokka's HTML renderer with one emitting a JSON description of the API; the site turns that into pages
with its own components under `site/api/`. Dokka does the Kotlin analysis and KDoc parsing; none of
its markup or styling reaches the site.

If a Dokka upgrade breaks that plugin it breaks as a compile error in `tools/dokka-json`, not as a
silently unstyled page. Bump `schema` in `ApiJsonRenderer` when the emitted shape changes
incompatibly, and the matching constant in `site/.vitepress/api/model.mts`.

Prose in `site/learn/` still needs a human to read it before a release; compiling proves the code
runs, not that the explanation is still true.

### Editing content without a checkout

[argot.draftcode.org/admin](https://argot.draftcode.org/admin) is [Sveltia
CMS](https://github.com/sveltia/sveltia-cms) — a visual editor that commits Markdown straight to
`main`. It owns news posts end to end and can edit prose in existing guides. It deliberately cannot
create a guide: guides transclude compiled regions from `docs-samples`, and a new example needs a
compiler.

It needs a one-time OAuth setup, because the browser cannot hold a GitHub secret:

1. Create a GitHub OAuth app (**Settings → Developer settings → OAuth Apps**) with the callback URL
   `https://<your-worker>.workers.dev/callback`.
2. Deploy [`sveltia-cms-auth`](https://github.com/sveltia/sveltia-cms-auth) to Cloudflare Workers,
   setting `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` from step 1, and `ALLOWED_DOMAINS` to
   `argot.draftcode.org`.
3. Put the worker's URL in `backend.base_url` in `site/public/admin/config.yml`.

Only the client secret is sensitive, and it lives in the worker rather than in this repository.

## Tests

Every behavioural change needs a test. The suites are:

- `argot-core` — parsing semantics, spec validation, and help rendering.
- `argot-processor` — real compilations through the KSP2 harness, covering both generated output and
  the compile errors the processor is expected to report.
- `sample` — end-to-end checks of both consumer styles.
