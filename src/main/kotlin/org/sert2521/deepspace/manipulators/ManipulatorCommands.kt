package org.sert2521.deepspace.manipulators

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.intake.Intake
import org.sert2521.deepspace.manipulators.intake.IntakeState
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Manipulators.releaseCurrent() = when (currentGamePiece) {
    GamePiece.CARGO -> use(Bucket) { Bucket.open() }
    else -> use(Claw) { Claw.release(true) }
}

suspend fun Manipulators.intakeCargo(extraTime: Double? = null) = use(Conveyor, Intake) {
    try {
        Intake.state = IntakeState.LOWERED

        periodic {
            Conveyor.setPercent()
            Intake.spin()

            if (hasCargoInConveyor && !Lift.atBottom) stop()
        }
    } finally {
        Intake.state = IntakeState.RAISED

        if (extraTime != null) {
            GlobalScope.launch(MeanlibDispatcher) {
                timer(extraTime) {
                    Conveyor.setPercent()
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
