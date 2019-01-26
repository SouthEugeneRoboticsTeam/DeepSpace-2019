package org.sert2521.deepspace.drivetrain

import org.team2471.frc.lib.motion_profiling.following.ArcadeRobot

internal const val DISTANCE_P = 0.075
internal const val DISTANCE_D = 0.003

internal val drivetrainConfig = ArcadeRobot.Config(
    trackWidth = 0.5,
    scrubFactor = 1.115,
    driveTurningP = 0.008,
    leftFeedForwardOffset = 0.02,
    leftFeedForwardCoefficient = 0.1,
    rightFeedForwardOffset = 0.02,
    rightFeedForwardCoefficient = 0.1,
    headingFeedForward = 0.0,
    doHeadingCorrection = true,
    headingCorrectionP = 0.0,
    headingCorrectionI = 0.0,
    headingCorrectionIDecay = 0.0
)
