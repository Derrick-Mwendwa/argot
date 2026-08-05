# Releasing Argot

Releases are driven entirely by git tags. The version lives in `build.gradle.kts`, and CI refuses to
publish if the tag and that version disagree.

## Versioning

Argot follows [Semantic Versioning](https://semver.org). Deciding which number to bump means asking
what changed, and Argot has **three** things a release can break:

1. **The source API** — everything `public` in `argot-core` and `argot-annotations`. What users type.
2. **The generated-code contract** — the core declarations that generated parsers call: `CommandSpec`,
   the three `ParamSpec` constructors, `ArgotEngine.parse`, and `ParsedValues.value`/`valueOrNull`/
   `flag`/`list`. This one is easy to break by accident, because nobody writes it by hand — but a
   user can have `argot-processor` 0.1.1 and `argot-core` 0.2.0 in the same build, and code emitted
   by the older processor still has to compile against the newer core.
3. **Observable CLI behaviour** — parsing semantics and the layout of `--help`. Someone's test
   asserts on that output.

Surfaces 1 and 2 are enforced by the build: `./gradlew build` runs `checkKotlinAbi`, which fails if
the public ABI drifts from the committed dumps in each module's `api/` directory. When a change to
them is deliberate, run `./gradlew updateKotlinAbi` and commit the updated dump alongside it.

**While Argot is `0.x`:**

| Bump | Contains |
|---|---|
| patch (`0.1.2`) | No change to any of the three surfaces. Docs, build, CI, internal refactors, generated-code *formatting*, and fixes to behaviour that was plainly wrong. |
| minor (`0.2.0`) | New features and additive API. Breaking changes are allowed, but each one needs a changelog entry with before/after. |
| `1.0.0` | The point at which breaking changes start costing a major bump. |

**After 1.0.0:** patch is fixes only, minor is additive only, and anything that removes or renames a
declaration, changes a signature, alters the generated-code contract, or changes parsing or `--help`
in a way that could break working code requires a major bump.

## Cutting a release

1. Make sure `main` is green.

2. On a branch, set the new version in `build.gradle.kts`:

   ```kotlin
   version = "0.1.1"
   ```

3. Move the `Unreleased` section of [CHANGELOG.md](CHANGELOG.md) under the new version and date it.

4. Open a pull request titled `Release 0.1.1`, and merge it once CI passes.

5. Tag the merge commit and push the tag:

   ```sh
   git switch main && git pull
   git tag v0.1.1
   git push origin v0.1.1
   ```

   That push is the release trigger. The `release` workflow then:

   - verifies `v0.1.1` matches the project version, failing loudly if not;
   - builds and tests every module;
   - uploads signed artifacts to the Central Portal;
   - creates the GitHub Release with generated notes.

6. **Approve the deployment.** Nothing is public until you do. Open
   [Deployments on the Central Portal](https://central.sonatype.com/publishing/deployments), check the
   validated deployment, and click **Publish**.

Artifacts usually appear on Maven Central within 15–30 minutes of that approval.

The GitHub Release is created as soon as the upload succeeds, which is *before* you approve. If you
drop a deployment rather than publishing it, delete the matching Release and tag as well.

To remove the approval step entirely, set `mavenCentralAutomaticPublishing=true` — then a tag push
goes straight through to a live, permanent artifact with nothing to catch a mistake.

## Repository secrets

The release workflow needs four secrets, injected as `ORG_GRADLE_PROJECT_*` environment variables.
To publish from your own machine instead, put the same values in `~/.gradle/gradle.properties` under
their Gradle property names (`mavenCentralUsername`, `mavenCentralPassword`, `signingInMemoryKey`,
`signingInMemoryKeyPassword`).

| Secret | Where it comes from |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | user token at [central.sonatype.com/account](https://central.sonatype.com/account) |
| `MAVEN_CENTRAL_PASSWORD` | the matching token password |
| `SIGNING_IN_MEMORY_KEY` | `gpg --armor --export-secret-keys <KEY_ID>` — stored with **real newlines** |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | the GPG key passphrase |

None of these are needed to build the project or to work on it locally.

**The signing key's line endings matter, and differ by channel.** In `~/.gradle/gradle.properties`
the key must be one line with `\n` escapes, because Java's properties loader converts those back to
real newlines. As a GitHub Secret it must keep its real newlines, because CI passes it through
`ORG_GRADLE_PROJECT_signingInMemoryKey` and nothing unescapes an environment variable. Getting this
backwards fails at the signing step, *after* a green build, with:

```
java.io.IOException: secret key ring doesn't start with secret key tag: tag 0xffffffff
```

To copy the key straight from your local properties file into the secret, converting as it goes:

```sh
grep -m1 '^signingInMemoryKey=' ~/.gradle/gradle.properties \
  | sed 's/^signingInMemoryKey=//' \
  | perl -pe 's/\\n/\n/g' \
  | gh secret set SIGNING_IN_MEMORY_KEY
```

## When a release goes wrong

**Before you approve on the Central Portal, nothing is public.** Drop the deployment there and the
version number stays free. Clean up and start over:

```sh
git tag -d v0.1.1
git push origin :refs/tags/v0.1.1
gh release delete v0.1.1 --yes
```

**After you approve, Maven Central is immutable.** That version can never be replaced or withdrawn.
A broken release is fixed forward with the next patch version — never by re-publishing the same one.

## Patching an older release line

Only needed when `main` has already moved on and an older line still needs a fix.

```sh
git switch -c release/0.1.x v0.1.1     # branch from the old tag
# ... fix, and set version = "0.1.2" ...
git tag v0.1.2
git push origin release/0.1.x v0.1.2
```

Then cherry-pick the fix back onto `main` so it isn't lost.
