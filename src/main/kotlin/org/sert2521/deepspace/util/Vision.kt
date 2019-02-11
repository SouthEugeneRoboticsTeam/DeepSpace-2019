package org.sert2521.deepspace.util

import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.RobotController.getFPGATime
import kotlin.math.cos
import kotlin.math.sin

private const val cameraToCenter = 13.25
private val lights = DigitalOutput(4)

enum class VisionSource(val path: String) {
    HatchPanel("hatch"), Cargo("cargo")
}

/**
 * An estimated pose for the camera relative to a target. All values are given in the robot's
 * coordinate system.
 *
 * @param xDistance the x distance away from the target
 * @param yDistance the y distance away from the target
 * @param robotAngle the angle between where the camera is facing and where the goal is
 * @param targetAngle the angle between a line perpendicular to the target and the camera
 *
 * @see https://www.chiefdelphi.com/t/a-step-by-step-run-through-of-frc-vision-processing/341012
 */
class CameraPose(
    val xDistance: Double,
    val yDistance: Double,
    val robotAngle: Double,
    val targetAngle: Double
)

abstract class Vision(source: VisionSource) {
    private val table = NetworkTableInstance.getDefault().getTable("Vision/${source.path}")
    private val defaultOffset = cameraToCenter - 0.0

    private val xDistance get() = table.getEntry("x_distance").getNumber(0.0).toDouble()
    private val yDistance get() = table.getEntry("y_distance").getNumber(0.0).toDouble()
    private val robotAngle get() = table.getEntry("robot_angle").getNumber(0.0).toDouble()
    private val targetAngle get() = table.getEntry("target_angle").getNumber(0.0).toDouble()

    private var lastUpdate = 0L

    /**
     * Whether or not the target is currently found.
     */
    val found get() = table.getEntry("found").getBoolean(false)

    /**
     * Whether the vision system is currently alive (has received value in last 2s).
     */
//    val alive get() = getFPGATime() - lastUpdate > 2.0e+6
    val alive = true

    /**
     * The time at which the last value was read. Note that this time is sent from the Jetson, so it
     * may not perfectly align with the roboRIO's clock.
     */
    val time get() = table.getEntry("time").getNumber(0.0).toLong()

    /**
     * The current estimated robot pose.
     */
    val pose get() = CameraPose(getXDistance(), getYDistance(), robotAngle, targetAngle)

    var locked = false
        set(value) {
            field = value
            println("Setting locked to $value")
//            table.getEntry("locked").setBoolean(value)
            // TODO: enable LEDs
        }

    private val hyp get() = xDistance / sin(Math.toRadians(robotAngle + targetAngle))

    /**
     * Gets the current estimated x distance from target using a specified [offset].
     *
     * @param offset the offset to use from the goal, typically 1/2 robot length + constant
     * @return the estimated y distance from camera
     */
    private fun getXDistance(offset: Double = defaultOffset) =
        sin(Math.toRadians(robotAngle + targetAngle)) * (hyp - offset)

    /**
     * Gets the current estimated y distance from target using a specified [offset].
     *
     * @param offset the offset to use from the goal, typically 1/2 robot length + constant
     * @return the estimated x distance from camera
     */
    private fun getYDistance(offset: Double = defaultOffset): Double {
        RobotController.getBatteryVoltage()
        val cosAngle = cos(Math.toRadians(robotAngle + targetAngle))
        return yDistance - cosAngle * hyp + cosAngle * (hyp - offset) + cameraToCenter
    }

    /**
     * Calculates the median camera pose over a specified [time], reading a value every [interval]
     * seconds. Note that for this to be accurate, the robot must not be moving during the time
     * period where calculations are taking place.
     *
     * This function will suspend its parent coroutine for [time] seconds while taking readings.
     *
     * @param time the length of time to read values
     * @param interval the rate at which values should be read
     * @param offset the offset to use from the goal, typically 1/2 robot length + constant
     * @return the median estimated [CameraPose] over the specified [time]
     */
    suspend fun getMedianPose(
        time: Double,
        interval: Double = 0.02,
        offset: Double = defaultOffset
    ): CameraPose {
        locked = true

        val xDistances = mutableListOf<Double>()
        val yDistances = mutableListOf<Double>()
        val robotAngles = mutableListOf<Double>()
        val targetAngles = mutableListOf<Double>()

        timer(time, interval) {
            xDistances.add(getXDistance(offset))
            yDistances.add(getYDistance(offset))
            robotAngles.add(robotAngle)
            targetAngles.add(targetAngle)
        }

        return CameraPose(
            xDistances.median(),
            yDistances.median(),
            robotAngles.median(),
            targetAngles.median()
        )
    }

    init {
        val flags = EntryListenerFlags.kImmediate or
            EntryListenerFlags.kNew or
            EntryListenerFlags.kUpdate

        table.addEntryListener({ _, _, _, _, _ ->
            lastUpdate = getFPGATime()
        }, flags)
    }

    companion object {
        // TODO: remove one of these if we're only using one camera
        object HatchPanel : Vision(VisionSource.HatchPanel)
        object Cargo : Vision(VisionSource.Cargo)

        var light: Boolean = false
            set(value) {
                field = value
                lights.set(!value)
            }

        fun getFromSource(source: VisionSource) = when (source) {
            VisionSource.HatchPanel -> HatchPanel
            VisionSource.Cargo -> Cargo
        }
    }
}
