package org.draftcode.argot

/**
 * Parses an argument list against a [CommandSpec].
 *
 * The engine never prints and never exits: `--help` and `--version` are reported as
 * [HelpRequested] and [VersionRequested], and invalid input as an [ArgotParseException]. Wrap a
 * call in [cli] for the usual behaviour of printing those and exiting.
 */
public object ArgotEngine {
    /**
     * Parses [argv] against [spec].
     *
     * @throws HelpRequested if `--help` or `-h` is present.
     * @throws VersionRequested if `--version` is present and [CommandSpec.version] is set.
     * @throws ArgotParseException if the input is invalid.
     * @throws IllegalArgumentException if [spec] itself is malformed.
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
            // Only an ArgotConversionException is treated as a deliberate explanation. Anything else
            // escaping a converter is a bug in it, and its message is written for a stack trace
            // rather than for someone's terminal.
            throw ArgotParseException.InvalidValue(
                name = name,
                raw = raw,
                expectedType = converter.typeName,
                detail = (e as? ArgotConversionException)?.message,
                cause = e,
            )
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
