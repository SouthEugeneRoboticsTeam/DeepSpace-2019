package org.sert2521.deepspace.drivetrain

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
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
import org.sert2521.deepspace.util.primaryController
import org.sert2521.deepspace.util.remap
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.motion.following.driveAlongPath
import org.team2471.frc.lib.motion.following.hybridDrive
import org.team2471.frc.lib.motion_profiling.Path2D
import kotlin.math.absoluteValue

private val throttle get() = primaryController.leftThumbstick.y.deadband(0.02)
private val turn get() = primaryController.rightThumbstick.x.deadband(0.02)
private val scale get() = 1.0 - primaryController.leftTrigger.deadband(0.02).remap(0.0..1.0, 0.0..0.5)

/**
 * Allows for teleoperated drive of the robot.
 */
suspend fun Drivetrain.teleopDrive() = use(this, name = "Teleop Drive") {
    periodic(watchOverrun = false) {
        val liftScalar = (1.0 - Lift.position / LiftState.HIGH.position).remap(0.0..1.0, 0.35..1.0)

        val scaledThrottle = throttle.remap(0.0..1.0, 0.0..scale) * liftScalar * -driveSpeedScalar
        val scaledTurn = turn.remap(0.0..1.0, 0.0..scale) * liftScalar * driveSpeedScalar

        Drivetrain.hybridDrive(scaledThrottle, 0.0, scaledTurn)
    }
}

suspend fun Drivetrain.drive(speed: Double) = use(this, name = "Drive") {
    periodic {
        Drivetrain.driveOpenLoop(speed, speed)
    }
}

suspend fun Drivetrain.driveTimed(time: Double, speed: Double) = use(this, name = "Timed Drive") {
    timer(time) {
        Drivetrain.driveOpenLoop(speed, speed)
    }
    Drivetrain.reset()
}

suspend fun Drivetrain.alignWithVision(source: VisionSource, alignOnly: Boolean = false) = use(this, name = "Vision Align") {
    val vision = Vision.getFromSource(source)

    val context = coroutineContext
    val cancelJob = launch(MeanlibDispatcher) {
        periodic {
            if (throttle.absoluteValue > 0.1 || turn.absoluteValue > 0.1) {
                vision.locked = false
                context.cancel()
            }
        }
    }

    // Pickup hatch panel if currently does not have game piece
    val shouldPickup = Manipulators.currentGamePiece == null

    vision.locked = true

    // Wait for light to turn on
    delay(0.2)

    val path = Path2D()

    suspend fun updatePath(time: Double, offset: Double) {
        if (!vision.found) return

        val pose = vision.getMedianPose(0.33, offset = offset)

        val xPosition = pose.xDistance / 12.0
        val yPosition = pose.yDistance / 12.0
        val angle = (pose.targetAngle + pose.robotAngle) * -1

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

//        println("""
//            ----------------------------------------------------
//            PATH UPDATED
//            ----------------------------------------------------
//            Length: ${path.length}, Tangent: ${path.getTangent(time)}
//            Head: ${path.xyCurve.headPoint}, Tail: ${path.xyCurve.tailPoint}
//            Duration (w/ Speed): ${path.durationWithSpeed}, (w/o Speed): ${path.duration}
//            Ease head: ${path.easeCurve.headKey}, Tail: ${path.easeCurve.tailKey}
//            Slope: $oldPath, ${path.easeCurve.getDerivative(time)}
//            ----------------------------------------------------
//        """.trimIndent())
    }

    updatePath(0.0, Vision.getOffset(24.0))

    driveAlongPath(path, extraTime = 0.1)

    if (!alignOnly) {
        updatePath(0.0, Vision.getOffset(0.0))

        vision.locked = false

        when {
            shouldPickup -> GlobalScope.parallel({ driveAlongPath(path, extraTime = 0.25) }, {
                delay(0.25)
                Claw.release(true) { !Drivetrain.followingPath }
            })
            else -> driveAlongPath(path, extraTime = 0.25)
        }
    }

    vision.locked = false

    cancelJob.cancelAndJoin()
}
