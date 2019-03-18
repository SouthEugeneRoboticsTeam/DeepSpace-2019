package org.sert2521.deepspace.climber

import badlog.lib.BadLog
import badlog.lib.DataInferMode
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.AnalogInput
import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.Servos
import org.sert2521.deepspace.util.Logger
import org.sert2521.deepspace.util.Servo
import org.sert2521.deepspace.util.Telemetry
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem

enum class ClimberState(val position: Double) {
    UP(-0.5 / 12.0),
    LEVEL_2(6.0 / 12.0),
    LEVEL_3(19.25 / 12.0)
}

object Climber : Subsystem("Climber") {
    private val telemetry = Telemetry(this)
    private val logger = Logger(this)

    private val frontLegs = MotorController(MotorControllers.CLIMBER_FRONT).config { brakeMode() }
    private val rearLegs = MotorController(
        MotorControllers.CLIMBER_LEFT_REAR,
        MotorControllers.CLIMBER_RIGHT_REAR
    ).config {
        inverted(true)
        ctreController.setNeutralMode(NeutralMode.Brake)
        ctreFollowers.forEach { it.setNeutralMode(NeutralMode.Coast) }
    }

    private val frontLock = Servo(Servos.CLIMBER_LOCK_RIGHT_FRONT, Servos.CLIMBER_LOCK_LEFT_FRONT).apply {
        invertFollowers = true
    }
    private val rearLock = Servo(Servos.CLIMBER_LOCK_RIGHT_REAR, Servos.CLIMBER_LOCK_LEFT_REAR).apply {
        invertPrimary = true
        invertFollowers = true
    }

    private val frontPot = AnalogInput(Sensors.CLIMBER_FRONT_POT)
    private val rearPot = AnalogInput(Sensors.CLIMBER_REAR_POT)

    private val frontLidar = AnalogInput(Sensors.CLIMBER_FRONT_LIDAR)
    private val rearLidar = AnalogInput(Sensors.CLIMBER_REAR_LIDAR)

    val frontLegPosition get() = (-0.0264591 * Math.pow(frontPot.averageValue.toDouble(), 0.858003) + 22.8938) / 12.0
    val rearLegPosition get() = (-0.0148318 * Math.pow(rearPot.averageValue.toDouble(), 0.926422) + 25.7399) / 12.0

    val frontOverStep get() = frontLidar.averageValue > 800
    val rearOverStep get() = rearLidar.averageValue > 800

    var frontLocked = false
        set(value) {
            field = value

            frontLock.set(if (value) LOCKED_POSITION else UNLOCKED_POSITION)
        }

    var rearLocked = false
        set(value) {
            field = value

            rearLock.set(if (value) LOCKED_POSITION else UNLOCKED_POSITION)
        }

    var locked
        get() = frontLocked && rearLocked
        set(value) {
            frontLocked = value
            rearLocked = value
        }

    init {
        telemetry.add("Front Leg Position") { frontLegPosition }
        telemetry.add("Rear Leg Position") { rearLegPosition }
        telemetry.add("Front LiDAR Value") { frontLidar.averageValue }
        telemetry.add("Rear LiDAR Value") { rearLidar.averageValue }

        logger.addNumberTopic("Front Leg Position") { frontLegPosition }
        logger.addNumberTopic("Rear Leg Position") { rearLegPosition }
        logger.addNumberTopic("Front LiDAR Value") { frontLidar.averageValue }
        logger.addNumberTopic("Rear LiDAR Value") { rearLidar.averageValue }
        logger.addSubscriber("Critical Events", BadLog.UNITLESS, DataInferMode.DEFAULT, "log")

        locked = true
    }

    fun logEvent(event: String) = logger.publish("Critical Events", event)

    fun setFrontSpeed(speed: Double) {
        // Unlock legs if attempting to retract
        if (speed <= 0) {
            frontLocked = false
        }

        frontLegs.setPercentOutput(speed)
    }

    fun setRearSpeed(speed: Double) {
        // Unlock legs if attempting to retract
        if (speed <= 0) {
            rearLocked = false
        }

        rearLegs.setPercentOutput(speed)
    }

    fun stopClimber() {
        locked = true

        rearLegs.stop()
        frontLegs.stop()
    }

    override fun reset() = stopClimber()
}

object ClimberDrive : Subsystem("Climber Drive") {
    private val drive = MotorController(MotorControllers.CLIMBER_DRIVE).config { inverted(true) }

    /**
     * Drives the climber at a constant speed.
     *
     * @param reverse whether the output should be reversed
     */
    fun driveOpenLoop(reverse: Boolean = false) {
        drive.setPercentOutput(CLIMBER_DRIVE_SPEED * if (reverse) -1 else 1)
    }

    /**
     * Stops all driving in the climber.
     */
    fun stop() = drive.stop()

    override fun reset() = stop()
}
