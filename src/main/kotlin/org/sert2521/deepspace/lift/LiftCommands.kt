package org.sert2521.deepspace.lift

import org.sert2521.deepspace.util.liftSpeedScalar
import org.sert2521.deepspace.util.secondaryJoystick
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Lift.manualControl() = use(this, name = "Manual Lift") {
    periodic(watchOverrun = false) {
        val speed = liftSpeedScalar * secondaryJoystick.y

        Lift.setSpeed(
            if (Lift.atTop && speed >= 0) 0.04 else speed
        )
    }
}

suspend fun Lift.elevateTo(state: LiftState) = use(this, name = "Elevate Lift") {
    Lift.followMotionCurve(
        calculateOptimalTime(Lift.position, state.position, MAX_ACCELERATION),
        state
    )
}
