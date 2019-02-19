package org.sert2521.deepspace.drivetrain

import edu.wpi.first.wpilibj.GenericHID
import kotlinx.coroutines.GlobalScope
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.lift.LiftState
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.util.Vision
import org.sert2521.deepspace.util.VisionSource
import org.sert2521.deepspace.util.addEasePointToEnd
import org.sert2521.deepspace.util.addPointToEnd
import org.sert2521.deepspace.util.driveSpeedScalar
import org.sert2521.deepspace.util.driverIsController
import org.sert2521.deepspace.util.driverIsJoystick
import org.sert2521.deepspace.util.primaryController
import org.sert2521.deepspace.util.primaryJoystick
import org.sert2521.deepspace.util.remap
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
        val liftModifier = (1.0 - Lift.position / LiftState.HIGH.position).remap(0.0..1.0, 0.25..1.0)

        when {
            driverIsController -> Drivetrain.hybridDrive(
                -driveSpeedScalar * liftModifier * primaryController.getY(GenericHID.Hand.kLeft).deadband(0.02),
                0.0,
                driveSpeedScalar * liftModifier * primaryController.getX(GenericHID.Hand.kRight).deadband(0.02)
            )
            driverIsJoystick -> Drivetrain.hybridDrive(
                -driveSpeedScalar * liftModifier * primaryJoystick.y.deadband(0.02),
                0.0,
                driveSpeedScalar * liftModifier * primaryJoystick.x.deadband(0.02)
            )
        }
    }
}

suspend fun Drivetrain.alignWithVision(source: VisionSource) = use(this) {
    // Pickup hatch panel if currently does not have game piece
    val shouldPickup = Manipulators.currentGamePiece == null

    val vision = Vision.getFromSource(source)

    vision.locked = true

    // Wait for light to turn on
    delay(0.2)

    val path = Path2D()

    var pose = vision.pose

    suspend fun updatePath(time: Double, offset: Double) {
        println("Alive? ${vision.alive}, Found? ${vision.found}")
        if (!vision.alive || !vision.found) return

        pose = vision.getMedianPose(0.33, offset = offset)

        val xPosition = pose.xDistance / 12.0
        val yPosition = pose.yDistance / 12.0
        val angle = (pose.targetAngle + pose.robotAngle) * -1

        println("X: $xPosition, Y: $yPosition, Target Angle: ${pose.targetAngle}, Robot Angle: ${pose.robotAngle}")

        val oldPath = path.easeCurve.getDerivative(time)
        var oldDuration = path.duration

        if (path.xyCurve.headPoint != null) path.xyCurve.removePoint(path.xyCurve.headPoint)
        if (path.xyCurve.tailPoint != null) path.xyCurve.removePoint(path.xyCurve.tailPoint)

        path.addPointToEnd(0.0, 0.0, angle = 0.0, magnitude = yPosition / 3.0)
        path.addPointToEnd(xPosition, yPosition, angle = angle, magnitude = yPosition / 3.0)

        if (oldDuration == 0.0) oldDuration = path.length / 4.0 + 1.0

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

    updatePath(0.0, 16.0 + 28.0)

    driveAlongPath(path, extraTime = 0.1)

    updatePath(0.0, 16.0 + 3.0)

    vision.locked = false

    when {
        shouldPickup -> GlobalScope.parallel({ driveAlongPath(path, extraTime = 0.25) }, {
            delay(0.25)
            Claw.release(true) { !Drivetrain.followingPath }
        })
        else -> driveAlongPath(path, extraTime = 0.25)
    }

    println("(${pose.xDistance}, ${pose.yDistance}, ${pose.robotAngle}, ${pose.targetAngle})")
}
