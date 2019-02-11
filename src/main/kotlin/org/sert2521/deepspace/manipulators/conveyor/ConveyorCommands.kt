package org.sert2521.deepspace.manipulators.conveyor

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Conveyor.run(extraTime: Double? = null) = use(this) {
    try {
        periodic {
            Conveyor.runSpeed()
        }
    } finally {
        if (extraTime != null) {
            GlobalScope.launch(MeanlibDispatcher) {
                timer(extraTime) {
                    Conveyor.runSpeed()
//                    if (Manipulators.hasCargoInConveyor && !Lift.atBottom) stop()
                }

                Conveyor.stop()
            }
        } else {
            Conveyor.stop()
        }
    }
}
