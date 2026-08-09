# What counts as a breaking change

Most libraries have one thing a release can break: the API you write against. Argot has three, and
knowing all three tells you how much risk an upgrade actually carries.

## 1. The source API

Everything `public` in `argot-core` and `argot-annotations` — the declarations you type. Renaming
`OptionBuilder.required()`, changing a parameter's type, removing an overload. This is the surface
every library has, and the obvious one.

## 2. The generated-code contract

The declarations that *generated* parsers call: `CommandSpec`, the three `ParamSpec` constructors,
`ArgotEngine.parse`, and `ParsedValues.value` / `valueOrNull` / `flag` / `list`.

You never write these by hand, so they look like internals. They are not. The processor and the core
are separate artifacts with separate version numbers, and nothing stops a build resolving
`argot-processor` 0.1.1 alongside `argot-core` 0.2.0 — a transitive dependency is enough. Code emitted
by the older processor still has to compile and link against the newer core.

A change here breaks builds that never mention the changed declaration, and the error points at
generated code you did not write. That is why these declarations are `public` and version-controlled
rather than hidden behind `internal`: mismatched versions of the two artifacts have to keep working
together.

## 3. Observable CLI behaviour

Parsing semantics and the layout of `--help`.

This one breaks nothing at compile time, which is what makes it easy to under-weigh. CLI programs get
tested by asserting on their output, so somewhere a test contains the exact `--help` text Argot
rendered. Changing the column at which help text aligns, the wording of an error, or whether
`--flag --flag` is tolerated will break that test on an upgrade you expected to be routine.

The first two surfaces are checked mechanically: the public ABI of each published module is recorded
and verified on every build, so neither can change without that being deliberate. The third has no
such check, and is held in place by tests over rendered help and error messages.

## What this means for version numbers

While Argot is `0.x`, breaking changes are allowed in a minor release, and each one is listed in the
[changelog](https://github.com/Derrick-Mwendwa/argot/blob/main/CHANGELOG.md) with before and after. A
patch release leaves all three surfaces alone.

After `1.0.0`, anything that removes or renames a declaration, changes a signature, alters the
generated-code contract, or changes parsing or `--help` in a way that could break working code
requires a major bump.

## Dependency updates are not automatically patches

The question is whether the update changes what a consumer must have or do.

The Gradle wrapper, test libraries and the publishing plugin are build-only — a consumer cannot
observe them, so they are patches. KotlinPoet is compile-time inside the processor, so it is a patch
unless the generated output changes.

Kotlin and KSP are not build-only. Annotation-style users must apply a matching Kotlin and KSP
version in their own build, so raising Argot's raises their floor. That is a minor release.

## See also

- [Changelog](https://github.com/Derrick-Mwendwa/argot/blob/main/CHANGELOG.md) — what actually
  changed in each release.
- [Two styles, one engine](./two-styles) for why surface 2 exists at all.
