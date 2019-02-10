package org.sert2521.deepspace.manipulators.claw

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.team2471.frc.lib.framework.Subsystem

object Claw : Subsystem("Claw") {
    internal enum class ClawState(val position: DoubleSolenoid.Value) {
        CLAMPED(DoubleSolenoid.Value.kReverse), RELEASED(DoubleSolenoid.Value.kForward)
    }

    private val solenoid = DoubleSolenoid(Pneumatics.CLAW_CLAMP, Pneumatics.CLAW_RELEASE)

    internal var state: ClawState = ClawState.CLAMPED
        set(value) {
            if (field != value) {
                field = value

                solenoid.set(value.position)
            }
        }

    override suspend fun default() {
        state = Claw.ClawState.CLAMPED
    }
}
