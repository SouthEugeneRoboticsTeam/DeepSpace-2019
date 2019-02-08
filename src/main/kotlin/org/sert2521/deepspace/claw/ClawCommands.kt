package org.sert2521.deepspace.claw

import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use

suspend fun Claw.release(suspend: Boolean = false) = use(this) {
    Claw.release()

    if (suspend) {
        suspendUntil { false }
    }
}
