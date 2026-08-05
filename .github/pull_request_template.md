## What this changes

<!-- One or two sentences. Why, not just what. -->

## Which surface does it touch?

<!-- See the Versioning section of RELEASING.md. Tick everything that applies. -->

- [ ] None — docs, build, CI, or internal refactor
- [ ] Source API (`public` in `argot-core` / `argot-annotations`)
- [ ] Generated-code contract (what generated parsers call)
- [ ] Observable CLI behaviour (parsing semantics or `--help` output)

If any of the last three are ticked, this is not a patch release. Say so in the changelog entry, and
run `./gradlew updateKotlinAbi` if the ABI dump needs updating.

## Checklist

- [ ] `./gradlew build` passes
- [ ] Behavioural changes have tests
- [ ] `CHANGELOG.md` updated under `Unreleased`
