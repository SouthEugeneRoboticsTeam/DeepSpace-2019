package org.sert2521.deepspace.manipulators.claw

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.team2471.frc.lib.framework.Subsystem

enum class ClawState(val position: DoubleSolenoid.Value) {
    CLAMPED(DoubleSolenoid.Value.kReverse), RELEASED(DoubleSolenoid.Value.kForward)
}

object Claw : Subsystem("Claw") {
    private val solenoid = DoubleSolenoid(Pneumatics.CLAW_CLAMP, Pneumatics.CLAW_RELEASE)

    var state: ClawState = ClawState.CLAMPED
        set(value) {
            if (field != value) {
                field = value

                solenoid.set(value.position)
            }
        }

    override suspend fun default() {
        state = ClawState.CLAMPED
    }
}
