# Choose between the delegate and annotation styles

Argot offers two ways to declare a command line. They compile to the same specification and produce
the same parsing and the same `--help`, so this is a question of what fits your codebase, not of
capability.

## The two, side by side

The delegate style is a class extending `Arguments`, with a property per parameter:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Greet.kt#declare{kotlin}

The annotation style is a `data class` whose constructor parameters carry annotations, with a parser
generated for it at build time by KSP:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Serve.kt#declare{kotlin}

## What actually differs

| | Delegate | Annotation |
|---|---|---|
| Artifacts | `argot-core` | `argot-core`, `argot-annotations`, `argot-processor` |
| Build setup | none | the KSP plugin |
| Errors in your declaration | when the parser is first built | while you compile |
| Result type | your `Arguments` subclass | a `data class` |
| Defaults | typed values — `.default(1)` | strings — `default = "1"` |
| Custom converters | `.convert(MyConverter)` | not available |

Two of those rows decide most cases.

**Custom converters are delegate-only.** The annotation style picks a converter from the parameter's
type, and there is no way to name your own. If you need a `Duration` or a validated path, you need
the delegate style — see [writing a converter](./custom-converter).

**Compile-time errors need the processor.** A duplicate option name or a positional in an impossible
position is reported by KSP while you build, whereas the delegate style raises it the first time the
parser runs. If your CLI is large enough that you would rather find that in CI than in a smoke test,
that is a real argument for annotations.

## What does not differ

The generated parser builds the same `CommandSpec` the delegates build. Help text, usage lines,
error messages, exit codes, `--help` and `--version` handling, and the rules for optionality are one
implementation, not two. See [two styles, one engine](../explanation/two-styles).

## A reasonable default

Start with the delegate style. It has one dependency, no build configuration, and nothing to learn
beyond the builder chain. Move to annotations when you want a `data class` you can pass around, or
when your declarations have grown big enough that catching mistakes at compile time is worth the KSP
setup.

Nothing stops you using both in one project — they are separate declarations that happen to share an
engine.

## See also

- [Two styles, one engine](../explanation/two-styles) for why they cannot drift apart.
- [`Arguments`](/api/org.draftcode.argot/Arguments) and the
  [annotations](/api/org.draftcode.argot.annotations/).
