package org.sert2521.deepspace.lift

import edu.wpi.first.wpilibj.InterruptHandlerFunction
import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.manipulators.GamePiece
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.util.Logger
import org.sert2521.deepspace.util.Telemetry
import org.sert2521.deepspace.util.timer
import org.sertain.hardware.DigitalInput
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion_profiling.MotionCurve
import kotlin.math.abs
import kotlin.math.sqrt

enum class LiftState(private val height: Double, private val gamePiece: GamePiece? = null) {
    HATCH_LOW(1.583, GamePiece.HATCH_PANEL),
    HATCH_MIDDLE(3.917, GamePiece.HATCH_PANEL),
    HATCH_HIGH(6.250, GamePiece.HATCH_PANEL),

    CARGO_LOW(2.292, GamePiece.CARGO),
    CARGO_MIDDLE(4.625, GamePiece.CARGO),
    CARGO_HIGH(7.225, GamePiece.CARGO),

    CARGO_SHIP(3.417, GamePiece.CARGO);

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

        val LOW get() = if (Manipulators.hasHatchPanel) HATCH_LOW else CARGO_LOW
        val MIDDLE get() = if (Manipulators.hasHatchPanel) HATCH_MIDDLE else CARGO_MIDDLE
        val HIGH get() = if (Manipulators.hasHatchPanel) HATCH_HIGH else CARGO_HIGH
    }
}

object Lift : Subsystem("Lift") {
    private val telemetry = Telemetry(this)
    private val logger = Logger(this)

    private val motor = MotorController(
        MotorControllers.LIFT_LEFT,
        MotorControllers.LIFT_RIGHT
    ).config {
        ctreController.configNeutralDeadband(0.0)

        ctreController.inverted = true
        ctreFollowers.forEach { it.inverted = false }

        brakeMode()
        sensorPhase(true)

        feedbackCoefficient = 1.0 / 7156.0

        // If current exceeds 15A for more than 5s, cut power back to 5A
        currentLimit(5, 15, 5)

        // Limit % output to [-0.35, 0.35]
        peakOutputRange(-0.35..0.35)
        nominalOutputRange(0.0..0.0)

        openLoopRamp(0.25)

        pid {
            p(DISTANCE_P)
            i(DISTANCE_I)
        }
    }

    private var motionCurve = MotionCurve()

    val position get() = motor.position

    val topSwitch = DigitalInput(Sensors.LIFT_SWITCH_TOP).invert()
    val bottomSwitch = DigitalInput(Sensors.LIFT_SWITCH_BOTTOM).invert()

    val atTop get() = topSwitch.get()
    val atBottom get() = bottomSwitch.get()

    init {
        telemetry.add("Position") { motor.position }
        telemetry.add("Current") { motor.current }
        telemetry.add("Top Switch") { atTop }
        telemetry.add("Bottom Switch") { atBottom }

        logger.addNumberTopic("Position") { motor.position }
        logger.addNumberTopic("Current") { motor.current }
        logger.addBooleanTopic("Top Switch") { atTop }
        logger.addBooleanTopic("Bottom Switch") { atBottom }

        bottomSwitch.requestInterrupts(object : InterruptHandlerFunction<Boolean>() {
            override fun interruptFired(interruptAssertedMask: Int, param: Boolean?) {
                motor.position = 0.0
            }
        })

        bottomSwitch.setUpSourceEdge(true, false)
        bottomSwitch.enableInterrupts()
    }

    fun calculateOptimalTime(currentPos: Double, targetPos: Double, accl: Double) =
        sqrt(abs(targetPos - currentPos) / (accl * 0.5))

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

    fun stop() = motor.stop()

    override fun reset() = stop()
}
