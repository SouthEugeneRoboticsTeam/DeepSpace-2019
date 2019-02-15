package org.sert2521.deepspace.drivetrain

import org.team2471.frc.lib.motion_profiling.following.ArcadeParameters
import org.team2471.frc.lib.motion_profiling.following.RobotParameters

internal const val DISTANCE_P = 0.00004789
internal const val DISTANCE_D = 0.008

internal const val MAX_VELOCITY = 10.0 // ft/s

internal val robotConfig = RobotParameters(
    robotWidth = 27.0 / 12.0,
    robotLength = 32.25 / 12.0
)

internal val drivetrainConfig = ArcadeParameters(
    trackWidth = 22.0 / 12.0,
    scrubFactor = 1.120,
    driveTurningP = 0.001,
    leftFeedForwardOffset = 0.0636475,
    leftFeedForwardCoefficient = 0.078196,
    rightFeedForwardOffset = 0.0636475,
    rightFeedForwardCoefficient = 0.078196,
    headingFeedForward = 0.0,
    doHeadingCorrection = true,
    headingCorrectionP = 0.015,
    headingCorrectionI = 0.005,
    headingCorrectionIDecay = 0.95
)
