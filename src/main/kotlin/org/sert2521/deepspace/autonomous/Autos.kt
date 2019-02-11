package org.sert2521.deepspace.autonomous

import org.sert2521.deepspace.drivetrain.Drivetrain
import org.team2471.frc.lib.motion.following.driveAlongPath

/**
 * This file defines the various different auto modes that can be run. They should call commands and
 * consist of minimal logic, instead deferring to their commands to handle any logic that might be
 * required.
 */

suspend fun testStraightAuto() {
    val auto = autonomi["Tests"]

    val path = auto["8 Foot Straight"]

    try {
        Drivetrain.driveAlongPath(path, 0.5)
    } finally {
        println("Done following path")
    }
}
