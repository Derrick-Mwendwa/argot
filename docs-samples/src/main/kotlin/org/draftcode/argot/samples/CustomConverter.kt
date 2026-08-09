package org.draftcode.argot.samples

// #region converter
import org.draftcode.argot.ArgotConversionException
import org.draftcode.argot.Arguments
import org.draftcode.argot.Converter

data class Duration(val seconds: Long)

object DurationConverter : Converter<Duration> {
    override val typeName: String = "Duration"

    override fun convert(raw: String): Duration {
        val match = Regex("""^(\d+)(s|m|h)$""").matchEntire(raw)
            ?: throw ArgotConversionException("'$raw' is not a duration (expected 30s, 5m, or 2h)")
        val (amount, unit) = match.destructured
        val multiplier = when (unit) {
            "s" -> 1L
            "m" -> 60L
            else -> 3600L
        }
        return Duration(amount.toLong() * multiplier)
    }
}
// #endregion converter

// #region use
class TimeoutArgs : Arguments(programName = "fetch", description = "Fetch a URL.") {
    val timeout: Duration by option("--timeout", "-t", help = "Give up after")
        .convert(DurationConverter)
        .default(Duration(seconds = 30))
}
// #endregion use
