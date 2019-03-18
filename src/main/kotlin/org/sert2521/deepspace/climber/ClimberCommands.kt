package org.sert2521.deepspace.climber

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.drive
import org.sert2521.deepspace.drivetrain.driveTimed
import org.sert2521.deepspace.util.PIDFController
import org.sert2521.deepspace.util.timer
import org.sert2521.deepspace.util.tol
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use

private suspend fun Climber.elevateWithPidTo(state: ClimberState) = use(Climber) {
    val frontPid = when (state) {
        ClimberState.LEVEL_2 -> PIDFController(kp = 0.57, ki = 0.0065, offset = 0.275)
        ClimberState.LEVEL_3 -> PIDFController(kp = 0.525, ki = 0.0065, offset = 0.275)
        else -> PIDFController()
    }
    val rearPid = when (state) {
        ClimberState.LEVEL_2 -> PIDFController(kp = 0.55, ki = 0.005, offset = 0.20)
        ClimberState.LEVEL_3 -> PIDFController(kp = 0.625, ki = 0.005, offset = 0.17)
        else -> PIDFController()
    }

    val highPowerScalar = when (state) {
        ClimberState.LEVEL_2 -> 0.75
        ClimberState.LEVEL_3 -> 0.6
        else -> 0.0
    }

    val frontOffset = when (state) {
        ClimberState.LEVEL_2 -> 1.0 / 12.0
        ClimberState.LEVEL_3 -> 2.0 / 12.0
        else -> 0.0
    }

    periodic {
        // Lower value to 80% of its PID output if leg is too high
        val frontScalar = if (frontLegPosition > rearLegPosition + 1.0 / 12.0) {
            highPowerScalar
        } else {
            1.0
        }
        val rearScalar = if (rearLegPosition > frontLegPosition + 1.0 / 12.0) {
            highPowerScalar
        } else {
            1.0
        }

        // Ensure the legs don't sink by forcing the value to be at least the PID holding offset
        val frontValue = Math.max(
            frontPid.update(state.position + frontOffset, frontLegPosition) * frontScalar,
            frontPid.offset
        )
        val rearValue = Math.max(
            rearPid.update(state.position, rearLegPosition) * rearScalar,
            rearPid.offset
        )

        if (rearLegPosition >= 0.0) setFrontSpeed(frontValue)
        setRearSpeed(rearValue)
    }
}

/**
 * Elevates front legs to an absolute potentiometer position.
 *
 * @param state desired potentiometer position
 */
suspend fun Climber.elevateFrontTo(state: ClimberState) = elevateFrontTo(state.position)

/**
 * Elevates front legs to an absolute potentiometer position.
 *
 * @param position desired potentiometer position
 */
suspend fun Climber.elevateFrontTo(position: Double) = use(this) {
    periodic {
        if (Climber.frontLegPosition !in (position tol ALLOWED_CLIMBER_ERROR)) {
            Climber.setFrontSpeed(if (Climber.frontLegPosition < position) CLIMBER_SPEED else -CLIMBER_SPEED)
        } else {
            Climber.setFrontSpeed(0.0)
            stop()
        }
    }
}

/**
 * Elevates rear legs to an absolute potentiometer position.
 *
 * @param state desired potentiometer position
 */
suspend fun Climber.elevateRearTo(state: ClimberState) = Climber.elevateRearTo(state.position)

/**
 * Elevates rear legs to an absolute potentiometer position.
 *
 * @param position desired potentiometer position
 */
suspend fun Climber.elevateRearTo(position: Double) = use(this) {
    periodic {
        if (Climber.rearLegPosition !in (position tol ALLOWED_CLIMBER_ERROR)) {
            Climber.setRearSpeed(if (Climber.rearLegPosition < position) CLIMBER_SPEED else -CLIMBER_SPEED)
        } else {
            Climber.setRearSpeed(0.0)
            stop()
        }
    }
}

suspend fun ClimberDrive.drive(reverse: Boolean = false) = use(this) {
    periodic {
        ClimberDrive.driveOpenLoop(reverse)
    }
}

suspend fun ClimberDrive.driveTimed(time: Double, reverse: Boolean = false) = use(this) {
    timer(time) {
        ClimberDrive.driveOpenLoop(reverse)
    }
}

suspend fun Climber.runClimbSequence(state: ClimberState) = use(Climber, ClimberDrive, Drivetrain) {
    Climber.logEvent("Elevating to ${state.name}")

    // Elevate the robot to the desired state
    val elevateRobot = launch(MeanlibDispatcher) { Climber.elevateWithPidTo(state) }

    // Wait for the legs to reach the desired state
    suspendUntil {
        Climber.frontLegPosition >= state.position - ALLOWED_CLIMBER_ERROR &&
        Climber.rearLegPosition >= state.position - ALLOWED_CLIMBER_ERROR
    }

    Climber.logEvent("Done elevating, driving")

    // Drive the robot forwards indefinitely
    val runForwardUntilRearLidar = launch(MeanlibDispatcher) {
        parallel(
            { Drivetrain.drive(-0.20) },
            { ClimberDrive.drive() }
        )
    }

    // Wait until the rear lidar is above the step
    suspendUntil { Climber.rearOverStep }

    // Cancel all driving and holding and prepare to lift legs
    runForwardUntilRearLidar.cancelAndJoin()
    elevateRobot.cancelAndJoin()

    Climber.logEvent("Retracting rear legs")

    // Raise the rear legs, while keeping the front at the desired state
    val raiseRear = launch(MeanlibDispatcher) {
        parallel(
            { Climber.elevateRearTo(ClimberState.UP) },
            { ClimberDrive.driveTimed(0.15, true) }
        )
    }

    // Wait until the rear leg is up
    suspendUntil { Climber.rearLegPosition <= 0 }

    Climber.logEvent("Done retracting rear legs, driving")

    // Drive forwards using the drivetrain and climber driveOpenLoop motor
    val runForwardUntilFrontLidar = launch(MeanlibDispatcher) {
        parallel(
            { Drivetrain.drive(-0.15) },
            { ClimberDrive.drive() }
        )
    }

    // Wait until the front lidar is above the step
    suspendUntil { Climber.frontOverStep }

    // Cancel all driving and holding and prepare to lift legs
    runForwardUntilFrontLidar.cancelAndJoin()
    raiseRear.cancelAndJoin()

    Climber.logEvent("Retracting front legs")

    // Raise front legs and jerk the drivetrain to ensure legs don't catch
    val raiseFront = launch(MeanlibDispatcher) {
        parallel(
            { Climber.elevateFrontTo(ClimberState.UP) },
            { Drivetrain.driveTimed(0.15, 0.15) }
        )
    }

    // Wait until the front legs are up
    suspendUntil { Climber.frontLegPosition <= 0 }

    // Stop raising
    raiseFront.cancelAndJoin()

    Climber.logEvent("Done retracting front legs, driving")

    // Finish driving onto platform
    parallel({
        Drivetrain.driveTimed(0.75, -0.35)
    }, {
        ClimberDrive.driveTimed(0.75)
    })

    Climber.logEvent("Climb complete")
}
