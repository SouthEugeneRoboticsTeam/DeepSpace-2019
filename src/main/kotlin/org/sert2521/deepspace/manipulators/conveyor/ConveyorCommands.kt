package org.sert2521.deepspace.manipulators.conveyor

import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Conveyor.run(invert: Boolean = false, override: Boolean = false) = use(this) {
    periodic {
        Conveyor.spin(CONVEYOR_SPEED * if (invert) -1 else 1)
        if (Manipulators.hasCargoInConveyor && !Lift.atBottom && !override) stop()
    }
}

suspend fun Conveyor.runTimed(time: Double, invert: Boolean = false) = use(this) {
    timer(time) {
        Conveyor.spin(CONVEYOR_SPEED * if (invert) -1 else 1)
        if (Manipulators.hasCargoInConveyor && !Lift.atBottom) stop()
    }
}
