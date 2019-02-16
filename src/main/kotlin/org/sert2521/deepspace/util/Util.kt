package org.sert2521.deepspace.util

import org.team2471.frc.lib.coroutines.PeriodicScope
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.util.Timer
import kotlin.math.abs
import kotlin.math.sqrt

class TimerScope internal constructor(var time: Double) {
    var periodicScope: PeriodicScope? = null

    val period = periodicScope?.period ?: 0.0
    fun stop() = periodicScope?.stop()
}

// Real-time timer utility, units are seconds
suspend fun timer(
    time: Double,
    delay: Double = 0.0,
    watchOverrun: Boolean = false,
    body: TimerScope.(Double) -> Unit
) {
    val scope = TimerScope(time)
    val timer = Timer().apply { start() }

    periodic(delay, watchOverrun) {
        if (scope.periodicScope == null) scope.periodicScope = this

        body(scope, timer.get())

        if (timer.get() >= scope.time) stop()
    }
}

fun List<Double>.median() = this.sorted().let {
    (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
}

// Function for tolerating error
infix fun Int.tol(error: Int) = (this - error)..(this + error)
infix fun Double.tol(error: Double) = (this - error)..(this + error)

// Function for finding fastest safe time to run lift
fun getOptimalTime(lastPos: Double, nextPos: Double, accl: Double) =
    sqrt(abs(nextPos - lastPos) / (.5 * accl))

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
