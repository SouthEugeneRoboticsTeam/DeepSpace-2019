package org.sert2521.deepspace.manipulators.intake

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Intake.run(extraTime: Double? = null) = use(this) {
    try {
        Intake.lower()
        periodic {
            Intake.runIntake()
        }
    } finally {
        Intake.raise()

        if (extraTime != null) {
            GlobalScope.launch(MeanlibDispatcher) {
                timer(extraTime) {
                    Intake.runIntake()
                }

                Intake.stop()
            }
        } else {
            Intake.stop()
        }
    }
}
