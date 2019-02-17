package org.sert2521.deepspace.autonomous

import kotlinx.coroutines.GlobalScope
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.alignWithVision
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.util.VisionSource
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.motion.following.driveAlongPath
import java.util.Date

/**
 * This file defines the various different auto modes that can be run. They should call commands and
 * consist of minimal logic, instead deferring to their commands to handle any logic that might be
 * required.
 */

suspend fun testStraightAuto() {
    val auto = autonomi["Left Start"]

    try {
        Drivetrain.driveAlongPath(auto["HAB to Rocket Front"], 0.1)

        Drivetrain.alignWithVision(VisionSource.Cargo)

        GlobalScope.parallel({
            delay(0.5)
            Drivetrain.driveAlongPath(auto["Rocket Front to Reverse"], 0.25)
        }, {
            val timeStart = Date().time
            Claw.release(true) { Date().time > timeStart + 1500 }
        })

        Drivetrain.driveAlongPath(auto["Rocket Reverse to Pickup"], 0.1)
    } finally {
        println("Done following path")
    }
}
