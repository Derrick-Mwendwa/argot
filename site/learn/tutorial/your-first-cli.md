# 1. Your first CLI

We are going to build `greet`, a small program that prints a greeting. By the end you will have
typed options, a flag, a default value, and a `--help` screen you did not write.

## Add the dependency

Argot's delegate style needs one artifact:

```kotlin
// build.gradle.kts
repositories { mavenCentral() }

dependencies {
    implementation("org.draftcode:argot-core:0.1.2")
}
```

## Declare what your program accepts

You describe the command line as a class. Each property is one parameter, and the delegate on the
right says what kind:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Greet.kt#declare{kotlin}

Three things are happening there:

- `option("--name", "-n")` takes a value. `.default("world")` means it is optional, and because a
  value is always present the property is a non-null `String`.
- `.int()` changes the type before `.default(1)` sets the fallback, so `count` is an `Int` and Argot
  rejects `--count banana` for you.
- `flag(...)` has no value. Its presence means `true`, its absence `false`.

## Use the parsed values

The class is ordinary Kotlin once parsed, so nothing here knows about Argot:

<<< ../../../docs-samples/src/main/kotlin/org/draftcode/argot/samples/Greet.kt#use{kotlin}

## Wire up `main`

`parsed` runs the parse and gives a command-line program its usual manners: on `--help` it prints the
help and exits `0`, and on bad input it prints the usage line and an error to stderr and exits `2`.

```kotlin
fun main(argv: Array<String>) {
    val args = GreetArgs().parsed(argv)
    greetings(args).forEach(::println)
}
```

## Run it

```console
$ greet
Hello, world!

$ greet --name Ada --count 2 --loud
HELLO, ADA!
HELLO, ADA!

$ greet -n Ada -c 3
Hello, Ada!
Hello, Ada!
Hello, Ada!
```

And the help screen, which you did not have to write:

```console
$ greet --help
Print a friendly greeting.

Usage: greet [options]

Options:
  --name, -n <String>   Who to greet (default: world)
  --count, -c <Int>     How many times (default: 1)
  --loud, -l            Shout the greeting
  -h, --help            Show this help message and exit
```

::: info Where these outputs come from
The declarations above are regions of a real file in the repository, and the outputs are asserted by
tests that run against the published release. They are not transcribed by hand.
:::

## What next

- [How-to guides](../how-to/) for specific tasks such as custom converters.
- [Explanation](../explanation/) for why there are two styles and what they share.
- The [API reference](../../api/) for exact signatures.
