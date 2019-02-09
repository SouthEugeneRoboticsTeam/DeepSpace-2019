package org.sert2521.deepspace.util

import org.team2471.frc.lib.coroutines.PeriodicScope
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.util.Timer

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
