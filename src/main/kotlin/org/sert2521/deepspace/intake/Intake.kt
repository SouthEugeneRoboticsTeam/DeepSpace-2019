package org.sert2521.deepspace.intake

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.sert2521.deepspace.Talons
import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    private enum class IntakeState {
        LOWERED, RAISED
    }

    private val solenoid = DoubleSolenoid(Pneumatics.KICKER_RAISE_CHANNEL, Pneumatics.KICKER_LOWER_CHANNEL)
    private val roller = TalonSRX(Talons.INTAKE_ROLLER)

    private var state: IntakeState = IntakeState.RAISED
        set(value) {
            // Ensure we only set solenoid once
            if (field == value) return

            when (value) {
                IntakeState.LOWERED -> solenoid.set(DoubleSolenoid.Value.kReverse)
                IntakeState.RAISED -> solenoid.set(DoubleSolenoid.Value.kForward)
            }

            field = value
        }

    fun intakeCargo() {
        state = IntakeState.LOWERED
        roller.setPercentOutput(ROLLER_SPEED)
    }

    fun stop() {
        state = IntakeState.RAISED
        roller.stop()
    }
}
