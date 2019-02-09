package org.sert2521.deepspace.lift

import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.util.timer
import org.sertain.hardware.DigitalInput
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion_profiling.MotionCurve

enum class LiftState(val position: Int) {
    BOTTOM(0), TOP(0),
    HATCH_LOW(0), HATCH_MIDDLE(0), HATCH_HIGH(0),
    CARGO_LOW(0), CARGO_MIDDLE(0), CARGO_HIGH(0),
}

object Lift : Subsystem("Lift") {
    private val motor = MotorController(
        MotorControllers.LIFT_LEFT,
        MotorControllers.LIFT_RIGHT
    ).config {
        ctreFollowers.forEach { it.inverted = true }

        peakOutput(0.25)
        brakeMode()

        closedLoopRamp(0.25)
        pid(0) {
            p(DISTANCE_P)
        }
    }

    private var motionCurve = MotionCurve()

    val position get() = motor.position

    val topSwitch = DigitalInput(Sensors.LIFT_SWITCH_TOP)
    val bottomSwitch = DigitalInput(Sensors.LIFT_SWITCH_BOTTOM)

    fun setSpeed(speed: Double) = motor.setPercentOutput(speed)

    fun setPosition(position: Double) = motor.setPositionSetpoint(position)

    suspend fun followMotionCurve(time: Double, position: Double) {
        motionCurve = MotionCurve()
        motionCurve.storeValue(0.0, this.position)
        motionCurve.storeValue(time, position)

        timer(motionCurve.maxValue) {
            setPosition(motionCurve.getValue(it))
        }
    }

    override suspend fun default() = Lift.manualControl()
}
