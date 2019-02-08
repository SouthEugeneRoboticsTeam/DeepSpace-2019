package org.sert2521.deepspace.claw

import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.sert2521.deepspace.Sensors
import org.team2471.frc.lib.framework.Subsystem

object Claw : Subsystem("Claw") {
    private enum class ClawState(val position: DoubleSolenoid.Value) {
        CLAMPED(DoubleSolenoid.Value.kReverse), RELEASED(DoubleSolenoid.Value.kForward)
    }

    private val solenoid = DoubleSolenoid(Pneumatics.CLAW_CLOSE, Pneumatics.CLAW_OPEN)
    private val switch = DigitalInput(Sensors.CLAW_SWITCH)

    private var state: ClawState = ClawState.CLAMPED
        set(value) {
            if (field != value) {
                field = value

                solenoid.set(value.position)
            }
        }

    val hasHatchPanel: Boolean get() = switch.get()

    fun clamp() {
        state = ClawState.CLAMPED
    }

    fun release() {
        state = ClawState.RELEASED
    }

    override suspend fun default() = clamp()
}
