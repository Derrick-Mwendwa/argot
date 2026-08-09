# Write a converter for your own type

Argot converts `String`, `Int`, `Long`, `Double`, `Boolean` and enums out of the box. For anything
else — a duration, a URL, a file path you want validated — you write a `Converter`.

## Implement `Converter<T>`

The interface is two members: a `typeName` shown in help and error messages, and a `convert` that
either returns a value or throws:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/CustomConverter.kt#converter{kotlin}

An `object` is usually right — a converter holds no per-parse state, so one instance can serve every
declaration that uses it.

## Attach it with `.convert(...)`

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/CustomConverter.kt#use{kotlin}

`.convert(...)` behaves like `.int()`: it fixes the value type, so `.default(...)` after it takes a
`Duration` rather than a `String`, and the property is a non-null `Duration`.

```console
$ fetch --timeout 5m      # args.timeout == Duration(seconds = 300)
$ fetch                   # args.timeout == Duration(seconds = 30)
```

## `typeName` is what users see

It appears in the help screen as the value placeholder:

```console
$ fetch --help
Options:
  --timeout, -t <Duration>   Give up after (default: Duration(seconds=30))
```

and in the message when a value is rejected:

```console
$ fetch --timeout soon
error: invalid value 'soon' for -t (expected Duration)
```

::: warning Your exception's message is not shown
Whatever you throw, the reported message is built from `typeName` alone — the text passed to
`ArgotConversionException` is discarded rather than chained. Put anything the user needs to see into
`typeName`.
:::

## Throw anything, but prefer `ArgotConversionException`

Any exception out of `convert` is treated as a rejected value, so `require(...)` and `toIntOrNull()
?: throw ...` both work. `ArgotConversionException` is the documented choice and states the intent
plainly to anyone reading your converter.

## See also

- [`Converter`](/api/org.draftcode.argot/Converter) and
  [`ArgotConversionException`](/api/org.draftcode.argot/ArgotConversionException).
- The built-in converters in [`org.draftcode.argot`](/api/org.draftcode.argot/) are small enough to
  read as examples.
