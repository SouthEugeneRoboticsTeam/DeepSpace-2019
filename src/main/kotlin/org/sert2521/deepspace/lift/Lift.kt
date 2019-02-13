package org.sert2521.deepspace.lift

import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.manipulators.GamePiece
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.util.Telemetry
import org.sert2521.deepspace.util.timer
import org.sertain.hardware.DigitalInput
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion_profiling.MotionCurve

enum class LiftState(private val height: Double, private val gamePiece: GamePiece? = null) {
    HATCH_LOW(1.583, GamePiece.HATCH_PANEL),
    HATCH_MIDDLE(3.917, GamePiece.HATCH_PANEL),
    HATCH_HIGH(6.250, GamePiece.HATCH_PANEL),

    CARGO_LOW(2.292, GamePiece.CARGO),
    CARGO_MIDDLE(4.625, GamePiece.CARGO),
    CARGO_HIGH(6.958, GamePiece.CARGO);

    /**
     * The absolute position the lift should be at.
     */
    val position get() = height - when (gamePiece) {
        GamePiece.HATCH_PANEL -> HATCH_PANEL_OFFSET
        GamePiece.CARGO -> CARGO_OFFSET
        else -> 0.0
    }

    companion object {
        val BOTTOM = HATCH_LOW
        val TOP = CARGO_HIGH

        val LOW = if (Manipulators.hasHatchPanel) HATCH_LOW else CARGO_LOW
        val MIDDLE = if (Manipulators.hasHatchPanel) HATCH_MIDDLE else CARGO_MIDDLE
        val HIGH = if (Manipulators.hasHatchPanel) HATCH_HIGH else CARGO_HIGH
    }
}

object Lift : Subsystem("Lift") {
    private val motor = MotorController(
        MotorControllers.LIFT_RIGHT,
        MotorControllers.LIFT_LEFT
    ).config {
        ctreController.configNeutralDeadband(0.0)
        ctreFollowers.forEach { it.inverted = true }

//        currentLimit()

        feedbackCoefficient = 1.0

//        peakOutput(0.25)
        brakeMode()

        closedLoopRamp(0.25)
        pid(0) {
            p(DISTANCE_P)
        }
    }

    val telemetry = Telemetry(this)

    init {
        telemetry.add("Position") { motor.position }
        telemetry.add("Top Switch") { topSwitch.get() }
        telemetry.add("Bottom Switch") { bottomSwitch.get() }
        telemetry.add("Current") { motor.current }
    }

    private var motionCurve = MotionCurve()

    val position get() = motor.position

    val topSwitch = DigitalInput(Sensors.LIFT_SWITCH_TOP).invert()
    val bottomSwitch = DigitalInput(Sensors.LIFT_SWITCH_BOTTOM).invert()

    val atTop get() = topSwitch.get()
    val atBottom get() = bottomSwitch.get()

    fun setSpeed(speed: Double) = motor.setPercentOutput(speed)

    fun setPosition(position: Double) = motor.setPositionSetpoint(position)

    suspend fun followMotionCurve(time: Double, state: LiftState) {
        motionCurve = MotionCurve()
        motionCurve.storeValue(0.0, position)
        motionCurve.storeValue(time, state.position)

        timer(motionCurve.maxValue) {
            setPosition(motionCurve.getValue(it))
        }
    }

    override suspend fun default() = Lift.manualControl()
}
