# Accept one of a fixed set of values

When an option may only be one of a handful of values, use a Kotlin `enum` and let Argot do the
matching. You get validation, a listing in the error message, and a real type on the other side.

## Use `.enum<E>()`

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Enums.kt#declare{kotlin}

The property is a `Level`, not a string you have to re-check:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Enums.kt#use{kotlin}

## Matching is already case-insensitive

You do not have to lowercase anything. All of these parse to `Level.WARN`:

```console
$ log --level warn
$ log --level WARN
$ log --level Warn
```

The comparison is against each constant's name, so the value the user types and the constant you
declared do not have to agree on case — only on spelling.

## Bad values list the valid ones

```console
$ log --level loud
error: invalid value for --level: 'loud' is not a valid Level (expected one of DEBUG, INFO, WARN, ERROR)
```

You do not have to repeat the constants in the help text — a rejected value names them all.

## Defaults are typed

`.default(Level.INFO)` takes a `Level`, not the string `"INFO"`, so a typo is a compile error rather
than a runtime surprise. The help screen renders it with the constant's name:

```console
$ log --help
Options:
  --level, -l <Level>  Minimum level (default: INFO)
```

## In the annotation style

The parameter's type selects the converter, so an enum parameter just works. Here `default` is a
string because annotation arguments must be compile-time constants. The generated code runs it
through the same converter when the parser is built, so a default that is not a valid constant fails
on the first parse rather than silently becoming something else:

```kotlin
@Command(name = "log", description = "Read the log.")
data class LogArgs(
    @Option(names = ["--level", "-l"], help = "Minimum level", default = "INFO")
    val level: Level,
)
```

## See also

- [`EnumConverter`](/api/org.draftcode.argot/EnumConverter) and
  [`enumConverter`](/api/org.draftcode.argot/).
- [Write a converter for your own type](./custom-converter) when a fixed set is not enough.
