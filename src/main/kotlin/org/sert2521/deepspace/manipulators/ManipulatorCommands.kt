package org.sert2521.deepspace.manipulators

import kotlinx.coroutines.Job
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.intake.Intake
import org.sert2521.deepspace.manipulators.intake.IntakeState
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.meanlibLaunch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

private var intakeJob: Job? = null
suspend fun Manipulators.intakeCargo(extraTime: Double? = null) = use(Conveyor, Intake, name = "Intake Cargo") {
    try {
        Intake.state = IntakeState.LOWERED

        periodic {
            Conveyor.spin()
            Intake.spin()

            if (hasCargoInConveyor && !Lift.atBottom) stop()
        }
    } finally {
        Intake.state = IntakeState.RAISED

        if (extraTime != null) {
            intakeJob?.cancel()
            intakeJob = meanlibLaunch {
                timer(extraTime) {
                    Conveyor.spin()
                    Intake.spin()

                    if (hasCargoInConveyor && !Lift.atBottom) stop()
                }

                Intake.stop()
                Conveyor.stop()
            }
        } else {
            Intake.stop()
            Conveyor.stop()
        }
    }
}
