package org.sert2521.deepspace.util

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.util.Timer

// Real-time timer utility, units are seconds
suspend fun timer(time: Double, delay: Double = 0.001, action: (time: Double) -> Unit) {
    val timer = Timer()
    while (timer.get() <= time) {
        action(timer.get())
        delay(delay)
    }
}
