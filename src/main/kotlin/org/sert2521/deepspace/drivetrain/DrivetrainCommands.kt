package org.sert2521.deepspace.drivetrain

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.GlobalScope
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.util.Vision
import org.sert2521.deepspace.util.VisionSource
import org.sert2521.deepspace.util.addEasePointToEnd
import org.sert2521.deepspace.util.addPointToEnd
import org.sert2521.deepspace.util.driveSpeedScalar
import org.sert2521.deepspace.util.primaryController
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion.following.hybridDrive
import org.team2471.frc.lib.motion_profiling.Path2D

/**
 * Allows for teleoperated drive of the robot.
 */
suspend fun Drivetrain.teleopDrive() = use(this) {
    periodic(watchOverrun = false) {
        Drivetrain.hybridDrive(
            -driveSpeedScalar * primaryController.getY(GenericHID.Hand.kLeft).deadband(0.02),
            0.0,
            driveSpeedScalar * primaryController.getX(GenericHID.Hand.kRight).deadband(0.02)
        )
    }
}

suspend fun Drivetrain.alignWithVision(source: VisionSource) = use(this) {
    val vision = Vision.getFromSource(source)

    vision.locked = true

    // Wait for light to turn on
    delay(0.1)

    val path = Path2D()

    var pose = vision.pose

    suspend fun updatePath(time: Double) {
        println("Alive? ${vision.alive}, Found? ${vision.found}")
        if (!vision.alive || !vision.found) return

        println(vision.pose)

        pose = vision.getMedianPose(0.25)

        println(pose)

        val xPosition = pose.xDistance / 12.0
        val yPosition = pose.yDistance / 12.0
        val angle = (pose.targetAngle + pose.robotAngle) * -1

        println("X: $xPosition, Y: $yPosition, Target Angle: ${pose.targetAngle}, Robot Angle: ${pose.robotAngle}")

        val tangent = path.getTangent(time)
        val oldPath = path.easeCurve.getDerivative(time)
        var oldDuration = path.duration

        if (path.xyCurve.headPoint != null) path.xyCurve.removePoint(path.xyCurve.headPoint)
        if (path.xyCurve.tailPoint != null) path.xyCurve.removePoint(path.xyCurve.tailPoint)

        path.addPointToEnd(0.0, 0.0, angle = 0.0, magnitude = yPosition / 3.0)
        path.addPointToEnd(xPosition, yPosition, angle = angle, magnitude = yPosition / 3.0)

        if (oldDuration == 0.0) oldDuration = path.length / 3.0 + 2.0

        path.addEasePointToEnd(time, 0.0, slope = oldPath, magnitude = 1.0)
        path.addEasePointToEnd(oldDuration, 1.0, slope = 0.0, magnitude = 1.0)

        path.easeCurve.headKey = path.easeCurve.getKey(time)
        path.easeCurve.tailKey = path.easeCurve.getKey(oldDuration)

        path.duration = oldDuration

        println("""
            ----------------------------------------------------
            PATH UPDATED
            ----------------------------------------------------
            Length: ${path.length}, Tangent: ${path.getTangent(time)}
            Head: ${path.xyCurve.headPoint}, Tail: ${path.xyCurve.tailPoint}
            Duration (w/ Speed): ${path.durationWithSpeed}, (w/o Speed): ${path.duration}
            Ease head: ${path.easeCurve.headKey}, Tail: ${path.easeCurve.tailKey}
            Slope: $oldPath, ${path.easeCurve.getDerivative(time)}
            ----------------------------------------------------
        """.trimIndent())
    }

    updatePath(0.0)

    GlobalScope.parallel({ driveAlongPath(path, extraTime = 0.5) }, {
        delay(0.5)
        Claw.release(true) {
            println(!Drivetrain.followingPath)
            !Drivetrain.followingPath
        }
    })

    println("(${pose.xDistance}, ${pose.yDistance}, ${pose.robotAngle}, ${pose.targetAngle})")

    vision.locked = false

    delay(10.0)
}
