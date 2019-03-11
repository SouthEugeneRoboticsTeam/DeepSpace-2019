package org.sert2521.deepspace.autonomous

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.alignWithVision
import org.sert2521.deepspace.manipulators.GamePiece
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.conveyor.runTimed
import org.sert2521.deepspace.util.VisionSource
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.motion.following.driveAlongPath
import java.util.Date

/**
 * This file defines the various different auto modes that can be run. They should call commands and
 * consist of minimal logic, instead deferring to their commands to handle any logic that might be
 * required.
 */

private suspend fun releaseHatchPanel() {
    GlobalScope.parallel({
        delay(0.5)
        timer(1.0) {
            Drivetrain.driveOpenLoop(-0.3, -0.3)
        }
    }, {
        val timeStart = Date().time
        Claw.release(true) { Date().time > timeStart + 1500 }
    })
}

private fun prepareLoadedCargo() {
    GlobalScope.launch(MeanlibDispatcher) {
        Conveyor.runTimed(1.0)
    }
}

suspend fun crossBaseline() {
    val auto = autonomi["Tests"]

    try {
        Drivetrain.driveAlongPath(auto["8 Foot Straight"])
    } finally {
        println("Done following path")
    }
}

suspend fun levelOneToRocket(start: AutoMode.StartPosition, pickup: Boolean) {
    val auto = autonomi["Paths"]

    auto.isMirrored = start.location == AutoMode.Location.RIGHT

    try {
        Drivetrain.driveAlongPath(auto["HAB to Rocket Front"], 0.2)

        Drivetrain.alignWithVision(VisionSource.Cargo)

        if (pickup) {
            GlobalScope.parallel({
                delay(0.5)
                Drivetrain.driveAlongPath(auto["Rocket Front to Reverse"], 0.2)
            }, {
                val timeStart = Date().time
                Claw.release(true) { Date().time > timeStart + 1500 }
            })

//            Drivetrain.driveAlongPath(auto["Rocket Reverse to Pickup"], 0.2)
        } else {
            releaseHatchPanel()
        }
    } finally {
        println("Done following path")
    }
}

suspend fun levelOneToCargoSide(start: AutoMode.StartPosition, gamePiece: GamePiece, pickup: Boolean) {
    val auto = autonomi["Paths"]

    auto.isMirrored = start.location == AutoMode.Location.RIGHT

    if (gamePiece == GamePiece.CARGO) prepareLoadedCargo()

    try {
        Drivetrain.driveAlongPath(auto["HAB to Cargo Side"], 0.1)

        Drivetrain.alignWithVision(VisionSource.Cargo)

        if (pickup) {
            GlobalScope.parallel({
                delay(0.5)
                Drivetrain.driveAlongPath(auto["Rocket Front to Reverse"], 0.1)
            }, {
                val timeStart = Date().time
                Claw.release(true) { Date().time > timeStart + 1500 }
            })

            Drivetrain.driveAlongPath(auto["Rocket Reverse to Pickup"], 0.1)
        } else {
            releaseHatchPanel()
        }
    } finally {
        println("Done following path")
    }
}

suspend fun levelOneToCargoFront(start: AutoMode.StartPosition, pickup: Boolean) {
    val auto = autonomi["Paths"]

    // Verify we are starting in the middle position
    if (start.location != AutoMode.Location.MIDDLE) return

    try {
        Drivetrain.driveAlongPath(auto["HAB to Cargo Front"], 0.1)

        Drivetrain.alignWithVision(VisionSource.Cargo)

        if (pickup) {
            GlobalScope.parallel({
                delay(0.5)
                Drivetrain.driveAlongPath(auto["Cargo Front to Reverse"], 0.1)
            }, {
                val timeStart = Date().time
                Claw.release(true) { Date().time > timeStart + 1500 }
            })

            Drivetrain.driveAlongPath(auto["Cargo Front Reverse to Pickup"], 0.1)
        } else {
            releaseHatchPanel()
        }
    } finally {
        println("Done following path")
    }
}
