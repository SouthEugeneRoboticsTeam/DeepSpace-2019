package org.sert2521.deepspace.manipulators.claw

import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use

suspend fun Claw.release(suspend: Boolean = false, finish: () -> Boolean = { false }) = use(this) {
    println("Releasing claw")
    state = Claw.ClawState.RELEASED

    if (suspend) {
        suspendUntil(condition = finish)
    }
}
