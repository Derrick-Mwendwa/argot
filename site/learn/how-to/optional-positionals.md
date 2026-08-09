# Make a positional argument optional

Positional arguments are required by default, and their order is the order you declare them. To let
one be omitted, mark it `.optional()`.

## Use `.optional()`

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Positional.kt#declare{kotlin}

`.optional()` makes the property nullable, which is the whole point: the type tells you the value
might not be there, so you cannot forget to handle it.

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Positional.kt#use{kotlin}

```console
$ copy notes.txt                  # destination == null
$ copy notes.txt backup.txt       # destination == "backup.txt"
```

The help screen shows the difference with brackets:

```console
$ copy --help
Usage: copy <source> [<destination>]

Arguments:
  <source>       File to copy
  <destination>  Where to put it
```

## Optional ones come last

An optional positional must follow the required ones. `copy [<destination>] <source>` has no
unambiguous reading — given one argument, the parser cannot know which slot it fills — so Argot
rejects that arrangement rather than guessing. The name comes from the property, so reordering the
declarations reorders the command line.

## Capturing the rest

For "one target and then any number of inputs", use `.multiple()` on the last positional:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Positional.kt#rest{kotlin}

```console
$ archive out.zip a.txt b.txt c.txt
# output == "out.zip", inputs == ["a.txt", "b.txt", "c.txt"]
```

`.multiple()` alone accepts zero inputs and gives an empty list; adding `.required()` demands at
least one. There can be at most one such trailing positional, and it must be declared last —
otherwise everything after it would be unreachable.

## In the annotation style

Nullability and `List` do the same work, so there is nothing extra to write:

```kotlin
@Command(name = "copy", description = "Copy a file.")
data class CopyArgs(
    @Argument(help = "File to copy") val source: String,
    @Argument(help = "Where to put it") val destination: String?,
)
```

## See also

- [`ArgumentBuilder`](/api/org.draftcode.argot/ArgumentBuilder) and
  [`OptionalArgumentBuilder`](/api/org.draftcode.argot/OptionalArgumentBuilder).
- [How optionality is inferred](../explanation/optionality) for why the property type is the source
  of truth.
