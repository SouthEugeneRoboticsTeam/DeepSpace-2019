package org.sert2521.deepspace.drivetrain

import edu.wpi.first.wpilibj.GenericHID
import org.sert2521.deepspace.util.driveSpeedScalar
import org.sert2521.deepspace.util.primaryController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.drive
import org.team2471.frc.lib.motion_profiling.following.driveAlongPath

/**
 * Allows for teleoperated drive of the robot.
 */
suspend fun Drivetrain.teleopDrive() = use(this) {
    periodic(watchOverrun = false) {
        Drivetrain.drive(
            driveSpeedScalar * primaryController.getY(GenericHID.Hand.kLeft),
            0.0,
            driveSpeedScalar * primaryController.getX(GenericHID.Hand.kRight)
        )
    }
}

suspend fun Drivetrain.followPath(path: Path2D, extraTime: Double = 0.0) = use(this) {
    Drivetrain.driveAlongPath(path, extraTime)
}
