package org.sert2521.deepspace.lift

import org.sert2521.deepspace.util.liftSpeedScalar
import org.sert2521.deepspace.util.secondaryJoystick
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Lift.manualControl() = use(this) {
    periodic(watchOverrun = false) {
        Lift.setSpeed(liftSpeedScalar * secondaryJoystick.y)
    }
}

suspend fun Lift.elevateTo(state: LiftState) = use(this) {
    // TODO: calculate time (target - current / velocity)
    Lift.followMotionCurve(1.0, state.position.toDouble())
}
