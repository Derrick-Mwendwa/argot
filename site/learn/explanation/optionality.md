# How optionality is inferred

Whether a parameter is required, optional, or defaulted is not something you declare twice in Argot.
You say it once, and the property's type follows — or, in the annotation style, the type says it and
Argot follows. Understanding which direction the inference runs explains most of the surprises.

## In the delegate style, the builder decides the type

A bare `option(...)` produces a nullable property. Each terminal call changes what you get:

| You write | Property type | Absent means |
|---|---|---|
| `option("--x")` | `String?` | `null` |
| `option("--x").required()` | `String` | a parse error |
| `option("--x").default("v")` | `String` | `"v"` |
| `option("--x").multiple()` | `List<String>` | empty list |
| `option("--x").multiple().required()` | `List<String>` | a parse error |
| `argument()` | `String` | a parse error |
| `argument().optional()` | `String?` | `null` |

This is why the builder returns a different type at each step rather than the same one: `.default(v)`
returns a `RequiredOptionBuilder<T>`, whose `provideDelegate` produces a non-null property. The
nullability of your property is a consequence of the call chain, so the two cannot disagree. You
never annotate a type and hope the library agrees with you.

The ordering rule falls out of the same idea. `.int()` says what one value is; `.default(1)` says
what to use when there is none. Writing `.default("1").int()` would not compile, because after
`.default(...)` there is no `.int()` to call.

## In the annotation style, the type decides

The processor reads the constructor parameter and infers:

- **A nullable type is optional.** `val host: String?` resolves to `null` when the option is absent.
- **A non-null type with no `default` is required.** `val port: Int` must be supplied.
- **A non-null type with `default = "..."` is optional.** The string is run through the same
  converter the parameter's type selects, so `default = "8080"` on an `Int` becomes `8080`.
- **A `List<T>` is repeatable and never required.** Absent means an empty list. A `List` cannot carry
  a `default`, and saying so is a compile error rather than a silently ignored annotation.

## What the processor cannot see

This produces the one error message that looks like a bug.

KSP tells the processor *that* a constructor parameter has a Kotlin default, but not what that
default is. Default values are expressions, and the processor has the declaration, not the compiled
expression. So this:

```kotlin
@Option(names = ["--port"], help = "Port") val port: Int = 8080
```

is rejected, with:

```
@Option 'port' has a Kotlin default that Argot cannot read.
Add default="..." to the annotation, or make the parameter type nullable.
```

The alternative would have been to ignore the Kotlin default and treat `port` as required — which
compiles, runs, and then fails at the command line for a user who reasonably expected 8080 to be the
default. Refusing to build is the less surprising of the two.

Write it as the processor can read it:

```kotlin
@Option(names = ["--port"], help = "Port", default = "8080") val port: Int
```

## Why not just read the Kotlin default anyway

Because the parser has to be able to *report* the default, not only apply it. `--help` shows
`(default: 8080)`, which means the value must be available to `CommandSpec` at the point the help is
rendered — before your constructor is called, and without evaluating an arbitrary expression that
might read a file or the clock. A string in the annotation is a value the processor can both pass
through the converter and print.

## See also

- [Make a positional argument optional](../how-to/optional-positionals) for the practical recipe.
- [`OptionBuilder`](/api/org.draftcode.argot/OptionBuilder) for the full chain of terminal calls.
