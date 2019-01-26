package org.sert2521.deepspace.drivetrain

import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.I2C
import org.sert2521.deepspace.Characteristics
import org.sert2521.deepspace.Talons
import org.sert2521.deepspace.util.Logger
import org.sert2521.deepspace.util.Telemetry
import org.team2471.frc.lib.actuators.TalonSRX
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion_profiling.following.ArcadeRobot

/**
 * The robot's drive system.
 */
object Drivetrain : Subsystem("Drivetrain", ::teleopDrive), ArcadeRobot {
    override val config = drivetrainConfig

    val leftDrive = TalonSRX(Talons.DRIVE_LEFT_FRONT, Talons.DRIVE_LEFT_REAR).config {
        feedbackCoefficient = ticksToFeet(1)
        brakeMode()
        inverted(true)
        closedLoopRamp(0.1)
        pid(0) {
            p(DISTANCE_P)
            d(DISTANCE_D)
        }
    }

    val rightDrive = TalonSRX(Talons.DRIVE_RIGHT_FRONT, Talons.DRIVE_RIGHT_REAR).config {
        feedbackCoefficient = ticksToFeet(1)
        brakeMode()
        closedLoopRamp(0.1)
        pid(0) {
            p(DISTANCE_P)
            d(DISTANCE_D)
        }
    }

    val telemetry = Telemetry(this)
    val logger = Logger(this)

    private val ahrs = AHRS(I2C.Port.kMXP)
    override val heading get() = ahrs.angle
    override val headingRate get() = ahrs.rate

    val speed: Double get() = (leftDrive.velocity + rightDrive.velocity) / 2.0

    val leftSpeed: Double get() = leftDrive.velocity

    val rightSpeed: Double get() = rightDrive.velocity

    val position: Double get() = (leftDrive.position + rightDrive.position) / 2.0

    val leftDistance get() = leftDrive.position

    val rightDistance get() = rightDrive.position

    val distance get() = (leftDistance + rightDistance) / 2.0

    init {
        telemetry.add("Gyro") { ahrs.angle }

        logger.addNumberTopic("Angle", "deg") { ahrs.angle }
        logger.addNumberTopic("Left Output") { leftDrive.output }
        logger.addNumberTopic("Right Output") { rightDrive.output }

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
    }

    fun coast() {
        leftDrive.config { coastMode() }
        rightDrive.config { coastMode() }
    }

    fun brake() {
        leftDrive.config { brakeMode() }
        rightDrive.config { brakeMode() }
    }
}
