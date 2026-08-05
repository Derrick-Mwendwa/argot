# Releasing Argot

Argot follows [Semantic Versioning](https://semver.org). While the version is `0.x`, a minor bump
may change the API; patch bumps never do.

Releases are driven entirely by git tags. The version lives in `build.gradle.kts`, and CI refuses to
publish if the tag and that version disagree.

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
- publishes signed artifacts to Maven Central;
- creates the GitHub Release with generated notes.

Artifacts usually appear on Maven Central within 15–30 minutes.

## Repository secrets

The release workflow needs four secrets. See `gradle.properties.template` for what each one is.

| Secret | Where it comes from |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | user token at [central.sonatype.com/account](https://central.sonatype.com/account) |
| `MAVEN_CENTRAL_PASSWORD` | the matching token password |
| `SIGNING_IN_MEMORY_KEY` | `gpg --armor --export-secret-keys <KEY_ID>`, newlines replaced with `\n` |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | the GPG key passphrase |

None of these are needed to build the project or to work on it locally.

## When a release goes wrong

**Maven Central is immutable.** A published version can never be replaced or withdrawn. If a release
is broken, fix forward with the next patch version — never try to re-publish the same one.

If the workflow fails *before* the publish step, delete the tag and try again:

```sh
git tag -d v0.1.1
git push origin :refs/tags/v0.1.1
```

If it fails *after* publishing, the artifacts are out. Cut `0.1.2`.

## Patching an older release line

Only needed when `main` has already moved on and an older line still needs a fix.

```sh
git switch -c release/0.1.x v0.1.1     # branch from the old tag
# ... fix, and set version = "0.1.2" ...
git tag v0.1.2
git push origin release/0.1.x v0.1.2
```

Then cherry-pick the fix back onto `main` so it isn't lost.
