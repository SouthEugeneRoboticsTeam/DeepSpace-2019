package org.sert2521.deepspace.drivetrain

import edu.wpi.first.wpilibj.GenericHID
import org.sert2521.deepspace.util.driveSpeedScalar
import org.sert2521.deepspace.util.primaryController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion_profiling.Path2D

/**
 * Allows for teleoperated driveRaw of the robot.
 */
suspend fun teleopDrive() = use(Drivetrain) {
    periodic(watchOverrun = false) {
        Drivetrain.drive(
            driveSpeedScalar * primaryController.getY(GenericHID.Hand.kLeft),
            driveSpeedScalar * primaryController.getX(GenericHID.Hand.kRight)
        )
    }
}

suspend fun followPath(path: Path2D, extraTime: Double = 0.0) = use(Drivetrain) {
    Drivetrain.driveAlongPath(path, extraTime)
}
