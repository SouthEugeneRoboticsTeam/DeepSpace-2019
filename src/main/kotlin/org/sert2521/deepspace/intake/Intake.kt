package org.sert2521.deepspace.intake

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.Pneumatics
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    private enum class IntakeState(val position: DoubleSolenoid.Value) {
        LOWERED(DoubleSolenoid.Value.kReverse), RAISED(DoubleSolenoid.Value.kForward)
    }

    private val solenoid = DoubleSolenoid(Pneumatics.INTAKE_RAISE, Pneumatics.INTAKE_LOWER)
    private val motor = MotorController(MotorControllers.INTAKE_LEFT, MotorControllers.INTAKE_RIGHT)

    private var state: IntakeState = IntakeState.RAISED
        set(value) {
            if (field != value) {
                field = value

                solenoid.set(value.position)
            }
        }

    fun intakeCargo() {
        state = IntakeState.LOWERED
        motor.setPercentOutput(ROLLER_SPEED)
    }

    fun stop() {
        state = IntakeState.RAISED
        motor.stop()
    }
}
