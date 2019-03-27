package org.sert2521.deepspace.autonomous

import kotlinx.coroutines.GlobalScope
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.alignWithVision
import org.sert2521.deepspace.manipulators.GamePiece
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.conveyor.runTimed
import org.sert2521.deepspace.util.VisionSource
import org.sert2521.deepspace.util.timer
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.meanlibLaunch
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
        timer(0.75) {
            Drivetrain.driveOpenLoop(-0.4, -0.4)
        }
    }, {
        val timeStart = Date().time
        Claw.release(true) { Date().time > timeStart + 1250 }
    })
}

private fun prepareLoadedCargo() {
    GlobalScope.meanlibLaunch {
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

suspend fun levelOneToRocketFront(start: AutoMode.StartPosition, pickup: Boolean) {
    val auto = autonomi["Paths"]

    auto.isMirrored = start.location == AutoMode.Location.RIGHT

    try {
        Drivetrain.driveAlongPath(auto["HAB to Rocket Front"], 0.2)

        Drivetrain.alignWithVision(VisionSource.Cargo, true)

        releaseHatchPanel()
    } finally {
        println("Done following path")
    }
}

suspend fun levelOneToRocketRear(start: AutoMode.StartPosition, pickup: Boolean) {
    val auto = autonomi["Paths"]

    auto.isMirrored = start.location == AutoMode.Location.RIGHT

    try {
        Drivetrain.driveAlongPath(auto["HAB to Rocket Rear"], 0.2)

        Drivetrain.alignWithVision(VisionSource.Cargo, true)

        releaseHatchPanel()
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

        Drivetrain.alignWithVision(VisionSource.Cargo, true)

        releaseHatchPanel()
    } finally {
        println("Done following path")
    }
}

suspend fun levelOneToCargoFront(start: AutoMode.StartPosition, pickup: Boolean) {
    val auto = autonomi["Paths"]

    // Verify we are starting in the middle position
    if (start.location != AutoMode.Location.MIDDLE) return

    try {
        Drivetrain.driveAlongPath(auto["Middle HAB to Left Cargo Front"], 0.1)

        Drivetrain.alignWithVision(VisionSource.Cargo, true)

        releaseHatchPanel()
    } finally {
        println("Done following path")
    }
}
