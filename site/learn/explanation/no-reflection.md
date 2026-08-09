# Why there is no runtime reflection

Most annotation-driven argument parsers read their annotations at startup: your program launches, the
library reflects over your class, discovers the fields, and builds a parser in memory. Argot does the
discovery at build time instead, and emits ordinary Kotlin.

## What that buys

**Mistakes surface while you compile.** A duplicate option name, a positional in an impossible
position, an unsupported parameter type — the processor reports these as compile errors with the
offending parameter underlined. A reflective parser cannot know about any of them until the program
runs, which usually means until a test runs, which sometimes means until a user runs it.

**Startup does no work.** There is no classpath scan and no annotation parsing at launch. For a
library about command-line programs this is not academic: CLIs are processes that start constantly,
often in scripts and loops, and a parser that costs tens of milliseconds before `main` gets going is
paying that on every invocation.

**Nothing is invisible to your tools.** The generated parser is real Kotlin in your build directory.
You can read it, step into it in a debugger, and see it in a stack trace. When something is wrong,
the thing you are debugging is code rather than a library's model of your code.

**It survives being shrunk.** Reflection makes your annotated classes reachable only through names,
so R8, ProGuard and GraalVM native-image all need to be told to keep them. Argot's generated parser
references your constructor directly, which is exactly the kind of edge a static analyser can follow.
No keep rules, no reflection configuration.

**Two dependencies weigh nothing at runtime.** `argot-core` and `argot-annotations` depend on the
Kotlin standard library and nothing else. KSP and KotlinPoet are build-time only.

## What it costs

**A build plugin.** Annotation-style users must apply KSP, and KSP is published against one specific
Kotlin version. That makes Argot's Kotlin version part of your build's constraints rather than an
implementation detail — which is why a Kotlin or KSP bump is a minor release for Argot, not a patch.

**Compile time, not startup time.** The work does not vanish; it moves. For a handful of commands
this is not measurable, but it is a real transfer rather than a free win.

**No dynamic declarations.** You cannot build a command line from a config file read at startup, or
add an option based on a runtime condition. Everything must be knowable at compile time. If that is
what you need, Argot's delegate style gets you closer — you can conditionally read properties — but
the declaration itself is still static.

**No custom converters in the annotation style.** The processor picks a converter from the
parameter's type, and there is no way to name one it has not been taught. A reflective library could
accept a `KClass` and instantiate it; the processor would have to generate a reference to something
it can resolve. Use the [delegate style](../how-to/custom-converter) when you need one.

## The delegate style is not reflective either

"No reflection" is usually said only about annotation processing, so to be explicit: Kotlin's
property delegates are a language feature, not a reflective one. `by option(...)` compiles to a field
holding the delegate and a getter that calls it. The `KProperty` passed to `provideDelegate` carries
the property's name, which is how a positional argument gets named after the property it is assigned
to — but that is a compiler-provided object, not a runtime lookup of your class.

## See also

- [Two styles, one engine](./two-styles) for what the processor actually generates.
- [How optionality is inferred](./optionality) for what the processor can and cannot see.
