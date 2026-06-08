package org.draftcode.argot

/**
 * The shared parsing engine. Both consumer styles build a [CommandSpec] and call [parse]; all
 * parsing, validation, and help rendering live here so behavior is identical across styles.
 *
 * The engine is pure and side-effect free: it never prints and never exits. `--help`/`-h` and
 * `--version` are surfaced as [HelpRequested]/[VersionRequested] signals, and user-input problems
 * as [ArgotParseException]s. The [cli] wrapper adapts those outcomes to stdout/stderr and a process
 * exit code.
 *
 * Parsing semantics (v1):
 * - Long options: `--name value` and `--name=value`.
 * - Short options: `-n value`, `-n=value`, attached `-nVALUE`, and combined flag clusters `-abc`
 *   (each cluster character must be a known flag, except a trailing option that takes a value).
 * - `--` terminates option parsing; every following token is positional.
 * - A repeated single-valued option is a [ArgotParseException.DuplicateValue]; a `multiple` option
 *   accumulates instead.
 * - Positionals are consumed left to right; a single trailing `multiple` positional captures the rest.
 */
public object ArgotEngine {
    /**
     * Parses [argv] against [spec] and returns the resolved [ParsedValues].
     *
     * @throws HelpRequested if `--help`/`-h` is present (carries fully rendered help text).
     * @throws VersionRequested if `--version` is present and [CommandSpec.version] is set.
     * @throws ArgotParseException for any user-input error; the exception's
     *   [ArgotParseException.usage] is populated before it escapes.
     * @throws IllegalArgumentException if [spec] itself is malformed (a programming error).
     */
    public fun parse(spec: CommandSpec, argv: Array<String>): ParsedValues {
        spec.validate()
        try {
            return doParse(spec, argv)
        } catch (e: ArgotParseException) {
            if (e.usage == null) e.usage = spec.renderUsage()
            throw e
        }
    }

    private fun doParse(spec: CommandSpec, argv: Array<String>): ParsedValues {
        val optionByName = HashMap<String, OptionSpec>()
        spec.options.forEach { opt -> opt.names.forEach { optionByName[it] = opt } }
        val flagByName = HashMap<String, FlagSpec>()
        val negationByName = HashMap<String, FlagSpec>()
        spec.flags.forEach { flag ->
            flag.names.forEach { flagByName[it] = flag }
            flag.negationNames.forEach { negationByName[it] = flag }
        }
        val knownNames: Set<String> = optionByName.keys + flagByName.keys + negationByName.keys

        // canonical name -> resolved value (single) or MutableList (multiple)
        val optionValues = HashMap<String, Any?>()
        val flagValues = HashMap<String, Boolean>()
        val positionalRaws = ArrayList<String>()

        var i = 0
        var afterTerminator = false
        while (i < argv.size) {
            val arg = argv[i]
            if (afterTerminator) {
                positionalRaws.add(arg); i++; continue
            }
            // `head` is the token without any `=value` suffix, so help/version are recognized
            // even when written as `--help=x` or `--version=x`.
            val head = arg.substringBefore('=')
            i = when {
                arg == "--" -> { afterTerminator = true; i + 1 }
                (head == "-h" || head == "--help") && head !in knownNames ->
                    throw HelpRequested(spec.renderHelp())
                head == "--version" && spec.version != null && head !in knownNames ->
                    throw VersionRequested(renderVersion(spec))
                arg.startsWith("--") ->
                    handleLong(arg, i, argv, optionByName, flagByName, negationByName, optionValues, flagValues)
                arg.length > 1 && arg.startsWith("-") ->
                    handleShort(arg, i, argv, spec, optionByName, flagByName, negationByName, optionValues, flagValues)
                else -> { positionalRaws.add(arg); i + 1 }
            }
        }

        val result = HashMap<String, Any?>()
        resolveOptions(spec, optionValues, result)
        resolveFlags(spec, flagValues, result)
        resolvePositionals(spec.arguments, positionalRaws, result)
        return ParsedValues(result)
    }

    private fun handleLong(
        arg: String,
        i: Int,
        argv: Array<String>,
        optionByName: Map<String, OptionSpec>,
        flagByName: Map<String, FlagSpec>,
        negationByName: Map<String, FlagSpec>,
        optionValues: MutableMap<String, Any?>,
        flagValues: MutableMap<String, Boolean>,
    ): Int {
        val eq = arg.indexOf('=')
        val name = if (eq >= 0) arg.substring(0, eq) else arg
        val inline = if (eq >= 0) arg.substring(eq + 1) else null
        val opt = optionByName[name]
        val flag = flagByName[name]
        val neg = negationByName[name]
        return when {
            opt != null -> {
                if (inline != null) {
                    recordOption(opt, name, inline, optionValues); i + 1
                } else {
                    if (i + 1 >= argv.size) throw ArgotParseException.MissingValue(name)
                    recordOption(opt, name, argv[i + 1], optionValues); i + 2
                }
            }
            flag != null -> {
                flagValues[flag.primaryName] = if (inline != null) parseFlagValue(name, inline) else true
                i + 1
            }
            neg != null -> {
                flagValues[neg.primaryName] = false; i + 1
            }
            else -> throw ArgotParseException.UnknownOption(name)
        }
    }

    private fun handleShort(
        arg: String,
        i: Int,
        argv: Array<String>,
        spec: CommandSpec,
        optionByName: Map<String, OptionSpec>,
        flagByName: Map<String, FlagSpec>,
        negationByName: Map<String, FlagSpec>,
        optionValues: MutableMap<String, Any?>,
        flagValues: MutableMap<String, Boolean>,
    ): Int {
        val body = arg.substring(1)
        var p = 0
        while (p < body.length) {
            val shortName = "-${body[p]}"
            val opt = optionByName[shortName]
            val flag = flagByName[shortName]
            val neg = negationByName[shortName]
            when {
                opt != null -> {
                    val rest = body.substring(p + 1)
                    return when {
                        rest.isEmpty() -> {
                            if (i + 1 >= argv.size) throw ArgotParseException.MissingValue(shortName)
                            recordOption(opt, shortName, argv[i + 1], optionValues)
                            i + 2
                        }
                        // `-n=value`: drop the '='. `-nVALUE`: take the rest verbatim.
                        rest.startsWith("=") -> { recordOption(opt, shortName, rest.substring(1), optionValues); i + 1 }
                        else -> { recordOption(opt, shortName, rest, optionValues); i + 1 }
                    }
                }
                flag != null -> {
                    flagValues[flag.primaryName] = true; p++
                }
                neg != null -> {
                    flagValues[neg.primaryName] = false; p++
                }
                // `--help`/`-h` is always available, including inside a flag cluster such as `-vh`.
                shortName == "-h" -> throw HelpRequested(spec.renderHelp())
                else -> throw ArgotParseException.UnknownOption(shortName)
            }
        }
        return i + 1
    }

    private fun recordOption(
        opt: OptionSpec,
        nameUsed: String,
        raw: String,
        optionValues: MutableMap<String, Any?>,
    ) {
        val key = opt.primaryName
        val converted = convert(opt.converter, nameUsed, raw)
        if (opt.multiple) {
            @Suppress("UNCHECKED_CAST")
            val list = optionValues.getOrPut(key) { ArrayList<Any?>() } as ArrayList<Any?>
            list.add(converted)
        } else {
            if (optionValues.containsKey(key)) throw ArgotParseException.DuplicateValue(nameUsed)
            optionValues[key] = converted
        }
    }

    private fun parseFlagValue(name: String, raw: String): Boolean =
        convert(BooleanConverter, name, raw) as Boolean

    private fun convert(converter: Converter<*>, name: String, raw: String): Any? =
        try {
            converter.convert(raw)
        } catch (e: Exception) {
            throw ArgotParseException.InvalidValue(name, raw, converter.typeName)
        }

    private fun resolveOptions(
        spec: CommandSpec,
        optionValues: Map<String, Any?>,
        result: MutableMap<String, Any?>,
    ) {
        spec.options.forEach { opt ->
            val key = opt.primaryName
            if (optionValues.containsKey(key)) {
                val v = optionValues[key]
                result[key] = if (opt.multiple) (v as List<*>).toList() else v
            } else if (opt.multiple) {
                if (opt.required) throw ArgotParseException.MissingRequiredOption(key)
                result[key] = emptyList<Any?>()
            } else {
                if (opt.required) throw ArgotParseException.MissingRequiredOption(key)
                result[key] = opt.default
            }
        }
    }

    private fun resolveFlags(
        spec: CommandSpec,
        flagValues: Map<String, Boolean>,
        result: MutableMap<String, Any?>,
    ) {
        spec.flags.forEach { flag ->
            result[flag.primaryName] = flagValues[flag.primaryName] ?: flag.default
        }
    }

    private fun resolvePositionals(
        arguments: List<ArgumentSpec>,
        raws: List<String>,
        result: MutableMap<String, Any?>,
    ) {
        var p = 0
        arguments.forEach { arg ->
            if (arg.multiple) {
                val rest = if (p < raws.size) raws.subList(p, raws.size) else emptyList()
                if (arg.required && rest.isEmpty()) throw ArgotParseException.MissingRequiredArgument(arg.name)
                result[arg.name] = rest.map { convert(arg.converter, arg.name, it) }
                p = raws.size
            } else if (p < raws.size) {
                result[arg.name] = convert(arg.converter, arg.name, raws[p]); p++
            } else {
                if (arg.required) throw ArgotParseException.MissingRequiredArgument(arg.name)
                result[arg.name] = null
            }
        }
        if (p < raws.size) throw ArgotParseException.TooManyArguments(raws.subList(p, raws.size).toList())
    }

    private fun renderVersion(spec: CommandSpec): String =
        if (spec.version != null) "${spec.programName} ${spec.version}" else spec.programName
}
