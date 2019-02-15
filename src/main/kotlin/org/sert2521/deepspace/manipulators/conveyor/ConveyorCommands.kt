package org.sert2521.deepspace.manipulators.conveyor

import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.manipulators.Manipulators
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Conveyor.run() = use(this) {
    try {
        periodic {
            Conveyor.setPercent()
            if (Manipulators.hasCargoInConveyor && !Lift.atBottom) stop()
        }
    } finally {
        Conveyor.stop()
    }
}
