package org.sert2521.deepspace.util

import org.team2471.frc.lib.coroutines.PeriodicScope
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.util.Timer
import java.awt.Color

class TimerScope internal constructor(var time: Double) {
    var periodicScope: PeriodicScope? = null

    val period = periodicScope?.period ?: 0.0
    fun stop() = periodicScope?.stop()
}

/**
 * Runs [body] for [time] seconds, with a specified [period].
 *
 * @param time the time to run for
 * @param period the time between each call
 * @param watchOverrun whether to watch for overrun of the specified [period]
 * @param body the method to call
 */
suspend fun timer(
    time: Double,
    period: Double = 0.0,
    watchOverrun: Boolean = false,
    body: TimerScope.(Double) -> Unit
) {
    val scope = TimerScope(time)
    val timer = Timer().apply { start() }

    periodic(period, watchOverrun) {
        if (scope.periodicScope == null) scope.periodicScope = this

        body(scope, timer.get())

        if (timer.get() >= scope.time) stop()
    }
}

/**
 * Calculates the median of a list of [Number].
 *
 * @return the median value
 */
fun List<Number>.median() = this.map { it.toDouble() }.sorted().let {
    (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
}

/**
 * Calculates a range from the specified number, plus or minus [error].
 *
 * @return a range of the specified number +/- error
 */
infix fun Int.tol(error: Int) = (this - error)..(this + error)

/**
 * Calculates a range from the specified number, plus or minus [error].
 *
 * @return a range of the specified number +/- error
 */
infix fun Double.tol(error: Double) = (this - error)..(this + error)

/**
 * Re-maps a number from a specified [fromRange] to a new [toRange] such that the smallest and
 * largest values in [fromRange] corresponds to the smallest and largest values in [toRange], and
 * the values between are mapped proportionally.
 *
 * @param fromRange the range that the initial value lies on
 * @param toRange the range to convert the value onto
 * @return the new value converted onto the [toRange]
 */
fun Number.remap(fromRange: DoubleRange, toRange: DoubleRange) =
    (this.toDouble() - fromRange.start) * (toRange.endInclusive - toRange.start) / (fromRange.endInclusive - fromRange.start) + toRange.start

/**
 * Calculates a color at a specified [percent] through a fade between a pair of [Color].
 *
 * @return the [Color] located at a specified [percent]
 */
fun Pair<Color, Color>.fade(percent: Double): Color {
    val red = Math.abs(percent * second.red + (1 - percent) * first.red)
    val green = Math.abs(percent * second.green + (1 - percent) * first.green)
    val blue = Math.abs(percent * second.blue + (1 - percent) * first.blue)

    return Color(red.toInt(), green.toInt(), blue.toInt())
}

/**
 * Format a specified [Double] to a specified [precision].
 *
 * @return the number truncated to a specified [precision]
 */
fun Double.format(precision: Int) = String.format("%.${precision}f", this)
