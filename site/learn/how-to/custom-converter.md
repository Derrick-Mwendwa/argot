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

## Your message is what the user sees

Throw `ArgotConversionException` and its message replaces the generic wording, because you know what
you wanted and the parser only knows the type's name:

```console
$ fetch --timeout soon
error: invalid value for -t: 'soon' is not a duration (expected 30s, 5m, or 2h)
```

Quote the offending value yourself, as the example above does — the parser does not repeat it.

## Throw anything, but prefer `ArgotConversionException`

Any exception out of `convert` is treated as a rejected value, so `require(...)` and `toIntOrNull()
?: throw ...` both work. Only `ArgotConversionException` has its message shown, though: anything else
escaping a converter is a bug in it, and its message is written for a stack trace rather than for
someone's terminal. Those fall back to the type name:

```console
$ fetch --timeout soon
error: invalid value 'soon' for -t (expected Duration)
```

Either way the original exception is kept as the `cause`, so nothing is lost from a stack trace.

## See also

- [`Converter`](/api/org.draftcode.argot/Converter) and
  [`ArgotConversionException`](/api/org.draftcode.argot/ArgotConversionException).
- The built-in converters in [`org.draftcode.argot`](/api/org.draftcode.argot/) are small enough to
  read as examples.
