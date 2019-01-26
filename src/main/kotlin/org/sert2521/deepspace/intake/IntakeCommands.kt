package org.sert2521.deepspace.intake

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Intake.runIntake() = use(this) {
    try {
        periodic(watchOverrun = false) {
            Intake.intakeCargo()
        }
    } finally {
        Intake.stop()
    }
}
