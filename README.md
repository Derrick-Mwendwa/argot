# Argot

[![Maven Central](https://img.shields.io/maven-central/v/org.draftcode/argot-core?label=Maven%20Central&color=blue)](https://central.sonatype.com/artifact/org.draftcode/argot-core)
[![build](https://github.com/Derrick-Mwendwa/argot/actions/workflows/build.yml/badge.svg)](https://github.com/Derrick-Mwendwa/argot/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

A small, zero-dependency Kotlin library for parsing command-line arguments. Declare your parameters
once and get typed, validated values at startup — inspired by Apple's
[swift-argument-parser](https://github.com/apple/swift-argument-parser).

Argot gives you **two ways to declare a CLI over one shared parsing engine**, so behaviour and
`--help` output are identical whichever you pick:

- **Delegate style** — plain Kotlin `by` delegates, no code generation. Only needs `argot-core`.
- **Annotation style** — annotate a data class and a KSP processor writes a typed parser at compile
  time. No runtime reflection.

```kotlin
class ServerArgs : Arguments(programName = "serve", description = "Run the server") {
    val host: String        by option("--host", help = "Bind host").default("0.0.0.0")
    val port: Int           by option("--port", "-p", help = "Port").int().required()
    val verbose: Boolean    by flag("--verbose", "-v", help = "Verbose logging")
    val files: List<String> by argument(help = "Files to serve").multiple()
}

fun main(argv: Array<String>) {
    val args = ServerArgs().parsed(argv)
    println("serving ${args.files} on ${args.host}:${args.port}")
}
```

## What Argot guarantees

- **Zero third-party runtime dependencies.** `argot-core` and `argot-annotations` depend only on the
  Kotlin standard library — no `kotlin-reflect`, no Guava, nothing.
- **No runtime reflection.** Delegates are a language feature; the annotation style emits explicit
  code at compile time.
- **Compile-time tooling stays at compile time.** KSP and KotlinPoet live only in `argot-processor`
  and never reach your runtime classpath.
- **Deterministic generated code** you can read, with an explicit API and JVM-first design that keeps
  a Kotlin Multiplatform port feasible.

## Installation

| Artifact | What it is | Needed for |
|---|---|---|
| `org.draftcode:argot-core` | Parsing engine and delegate-style API | both styles |
| `org.draftcode:argot-annotations` | `@Command`, `@Option`, `@Flag`, `@Argument` | annotation style |
| `org.draftcode:argot-processor` | KSP2 processor, compile-time only | annotation style |

### Delegate style

```kotlin
repositories { mavenCentral() }

dependencies {
    implementation("org.draftcode:argot-core:0.1.1")
}
```

Declare parameters with `option`, `flag`, and `argument` as in the example above. The builder DSL
takes `.int()`, `.long()`, `.double()`, `.boolean()`, `.enum<E>()`, and `.convert(...)` for the value
type, then `.default(...)`, `.required()`, `.multiple()`, or `.optional()` for cardinality.

The call you end with drives the property type: a bare `option(...)` backs a nullable property, while
`.required()` and `.default(...)` back a non-null one. Reading a property before parsing fails fast.

### Annotation style

```kotlin
plugins {
    kotlin("jvm") version "2.3.20"
    id("com.google.devtools.ksp") version "2.3.9"
}

repositories { mavenCentral() }

dependencies {
    implementation("org.draftcode:argot-core:0.1.1")
    implementation("org.draftcode:argot-annotations:0.1.1")
    ksp("org.draftcode:argot-processor:0.1.1")
}
```

```kotlin
@Command(name = "serve", description = "Run the server")
data class ServeArgs(
    @Option(names = ["--host"], help = "Bind host", default = "0.0.0.0") val host: String,
    @Option(names = ["--port", "-p"], help = "Port") val port: Int,        // required: non-null, no default
    @Flag(names = ["--verbose", "-v"], help = "Verbose") val verbose: Boolean = false,
    @Argument(help = "Files to serve") val files: List<String>,            // trailing positional list
)

fun main(argv: Array<String>) {
    val args = cli { parseServeArgs(argv) }   // parseServeArgs is generated
    println("serving ${args.files} on ${args.host}:${args.port}")
}
```

The processor generates a sibling `parseServeArgs(argv: Array<String>): ServeArgs`; your class is
never modified. Optionality comes from what KSP can see: a non-nullable parameter with no `default`
is required, a nullable type is optional and resolves to `null`, `@Option(default = "...")` is
optional with that default, and a `List<T>` parameter is a trailing positional or repeatable option.

Duplicate names, misplaced positionals, `@Flag` on a non-`Boolean`, unsupported types, and Kotlin
defaults Argot cannot read are all reported as clear compile errors.

## `--help` output

Both styles render the same aligned help:

```
Run the server

Usage: serve [options] [<files>...]

Options:
  --host <String>   Bind host (default: 0.0.0.0)
  --port, -p <Int>  Port (required)
  --verbose, -v     Verbose logging
  -h, --help        Show this help message and exit

Arguments:
  <files>           Files to serve
```

## Parsing semantics

- Long options: `--name value` and `--name=value`.
- Short options: `-n value`, `-n=value`, attached `-nVALUE`, and combined flag clusters `-abc`.
- `--` terminates option parsing; everything after it is positional.
- A repeated single-valued option is an error; a `multiple` option accumulates into a list.
- Positionals are consumed left to right; one trailing `multiple` positional captures the rest.
- `--help` and `-h` are always recognized; `--version` when a version is configured.

`ArgotEngine.parse(spec, argv)` is pure — it never prints and never exits. `--help` and `--version`
surface as `HelpRequested` and `VersionRequested`, and bad input as a sealed `ArgotParseException`.
The `cli { ... }` wrapper (and `Arguments.parsed`) turn those into console output and an exit code.

## Compatibility

Argot pins the latest mutually compatible **Kotlin 2.3.20 / KSP 2.3.9 / KotlinPoet 2.3.0** set, on a
JDK 17 toolchain. If you use the annotation style, apply a matching Kotlin and KSP version.

## Building from source

```sh
./gradlew build      # compiles every module and runs all tests
./gradlew :sample:run --args="greet --name Ada --count 2 --shout"
./gradlew :sample:run --args="serve --port 8080 a.txt b.txt"
```

The `sample` module demonstrates both styles in one app and doubles as an integration test.

## Not in scope yet

Subcommands and nested commands, Kotlin Multiplatform targets, shell-completion generation, custom
converters by annotation, environment-variable fallback, and honouring unread Kotlin constructor
default literals.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the branching model and code conventions, and
[RELEASING.md](RELEASING.md) for how releases are cut. Changes are recorded in
[CHANGELOG.md](CHANGELOG.md).

## License

[Apache License 2.0](LICENSE).
