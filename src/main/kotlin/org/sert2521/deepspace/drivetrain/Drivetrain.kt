package org.sert2521.deepspace.drivetrain

import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.I2C
import org.sert2521.deepspace.Characteristics
import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.util.Logger
import org.sert2521.deepspace.util.Telemetry
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion.following.ArcadeDrive

/**
 * The robot's drive system.
 */
object Drivetrain : Subsystem("Drivetrain"), ArcadeDrive {
    override val parameters = drivetrainConfig

    private val leftDrive = MotorController(
        MotorControllers.DRIVE_LEFT_FRONT,
        MotorControllers.DRIVE_LEFT_REAR
    ).config {
        feedbackCoefficient = ticksToFeet(1)
        brakeMode()
        sensorPhase(true)
        closedLoopRamp(0.1)
        pid(0) {
            p(DISTANCE_P)
            d(DISTANCE_D)
        }
    }

    private val rightDrive = MotorController(
        MotorControllers.DRIVE_RIGHT_FRONT,
        MotorControllers.DRIVE_RIGHT_REAR
    ).config {
        feedbackCoefficient = ticksToFeet(1)
        brakeMode()
        inverted(true)
        sensorPhase(true)
        closedLoopRamp(0.1)
        pid(0) {
            p(DISTANCE_P)
            d(DISTANCE_D)
        }
    }

    private val telemetry = Telemetry(this)
    private val logger = Logger(this)

    private val ahrs = AHRS(I2C.Port.kMXP)
    override val heading get() = ahrs.angle
    override val headingRate get() = ahrs.rate

    var followingPath = false

    val leftSpeed: Double get() = leftDrive.velocity

    val rightSpeed: Double get() = rightDrive.velocity

    val speed: Double get() = (leftSpeed + rightSpeed) / 2.0

    val leftDistance get() = leftDrive.position

    val rightDistance get() = rightDrive.position

    val distance get() = (leftDistance + rightDistance) / 2.0

    init {
        telemetry.add("Gyro") { ahrs.angle }
        telemetry.add("Left Distance") { leftDistance }
        telemetry.add("Right Distance") { rightDistance }
        telemetry.add("Following Path") { followingPath }

        logger.addNumberTopic("Angle", "deg") { ahrs.angle }
        logger.addBooleanTopic("Following Path") { followingPath }
        logger.addNumberTopic("Left Output", "%", "hide", "join:Drivetrain/Percent") {
            leftDrive.output
        }
        logger.addNumberTopic("Right Output", "%", "hide", "join:Drivetrain/Percent") {
            rightDrive.output
        }
        logger.addNumberTopic("Left Distance", "ft", "hide", "join:Drivetrain/Distances") {
            leftDistance
        }
        logger.addNumberTopic("Right Distance", "ft", "hide", "join:Drivetrain/Distances") {
            rightDistance
        }

        zeroEncoders()
        zeroGyro()
    }

    fun zeroEncoders() {
        leftDrive.position = 0.0
        rightDrive.position = 0.0
    }

    fun zeroGyro() {
        ahrs.reset()
    }

    fun ticksToFeet(ticks: Int) =
        ticks.toDouble() / Characteristics.ENCODER_TICKS_PER_REVOLUTION * Characteristics.WHEEL_DIAMETER * Math.PI / 12.0

    fun feetToTicks(feet: Double) =
        feet * 12.0 / Math.PI / Characteristics.WHEEL_DIAMETER * Characteristics.ENCODER_TICKS_PER_REVOLUTION

    override fun driveOpenLoop(leftPower: Double, rightPower: Double) {
        leftDrive.setPercentOutput(leftPower)
        rightDrive.setPercentOutput(rightPower)
    }

    override fun driveClosedLoop(
        leftDistance: Double,
        leftFeedForward: Double,
        rightDistance: Double,
        rightFeedForward: Double
    ) {
        leftDrive.setPositionSetpoint(leftDistance, leftFeedForward)
        rightDrive.setPositionSetpoint(rightDistance, rightFeedForward)
    }

    override fun startFollowing() {
        zeroEncoders()
        zeroGyro()

        followingPath = true
    }

    override fun stopFollowing() {
        followingPath = false
    }

    fun coast() {
        leftDrive.config { coastMode() }
        rightDrive.config { coastMode() }
    }

    fun brake() {
        leftDrive.config { brakeMode() }
        rightDrive.config { brakeMode() }
    }

    override suspend fun default() = Drivetrain.teleopDrive()

    override fun reset() = stop()
}
