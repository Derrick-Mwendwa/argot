# Collect a repeated option

You want `--tag beta --tag eu` to give you both values rather than the last one.

## Use `.multiple()`

`.multiple()` changes the property's type to `List<T>` and tells the parser to accumulate instead of
replace:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Repeated.kt#declare{kotlin}

```console
$ tag --tag beta -t eu --env staging
tags=beta,eu envs=staging
```

Values arrive in the order they were supplied, and short and long names are interchangeable — `-t eu`
and `--tag eu` land in the same list.

## Absent means empty, not null

A `.multiple()` property is never null. Supply the option zero times and you get an empty list, so
you can iterate without a null check:

```kotlin
for (tag in args.tag) apply(tag)
```

That is why `.multiple()` on its own is *not* the same as "optional". If you need at least one value,
say so:

```kotlin
val env: List<String> by option("--env", "-e", help = "Environment").multiple().required()
```

Now omitting `--env` entirely is a parse error, while `--env prod` is fine.

## Convert before you accumulate

Order matters: refine the type first, then make it repeatable. `.int()` describes what one value is,
`.multiple()` describes how many there are.

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Repeated.kt#typed{kotlin}

```console
$ expose -p 80 -p 443     # args.port == listOf(80, 443)
```

Every value goes through the converter, so `-p 80 -p https` fails on the second one with the usual
message rather than silently producing a partial list.

## In the annotation style

Declare the parameter as a `List<T>` and the processor infers the same behaviour:

```kotlin
@Command(name = "tag", description = "Apply tags to a build.")
data class TagArgs(
    @Option(names = ["--tag", "-t"], help = "Tag to apply") val tag: List<String>,
)
```

## See also

- [Accept one of a fixed set of values](./enum-values) — repeatable enums work the same way.
- [`OptionListBuilder`](/api/org.draftcode.argot/OptionListBuilder) for the exact signatures.
