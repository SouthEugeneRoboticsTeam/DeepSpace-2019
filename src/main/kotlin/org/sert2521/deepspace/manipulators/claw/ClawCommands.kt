package org.sert2521.deepspace.manipulators.claw

import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use

suspend fun Claw.release(suspend: Boolean = false, finish: () -> Boolean = { false }) = use(this, name = "Release Claw") {
    state = ClawState.RELEASED

    if (suspend) {
        suspendUntil(condition = finish)
    }
}

suspend fun Claw.clamp(suspend: Boolean = false, finish: () -> Boolean = { false }) = use(this, name = "Clamp Claw") {
    state = ClawState.CLAMPED

    if (suspend) {
        suspendUntil(condition = finish)
    }
}
