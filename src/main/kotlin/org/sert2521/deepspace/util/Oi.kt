package org.sert2521.deepspace.util

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.Preferences
import edu.wpi.first.wpilibj.XboxController
import org.sert2521.deepspace.Operator
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.alignWithVision
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.lift.LiftState
import org.sert2521.deepspace.lift.elevateTo
import org.sert2521.deepspace.lift.manualControl
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.conveyor.run
import org.sert2521.deepspace.manipulators.intakeCargo
import org.team2471.frc.lib.framework.aPress
import org.team2471.frc.lib.framework.createMappings
import org.team2471.frc.lib.framework.leftBumperHold
import org.team2471.frc.lib.framework.rightBumperHold
import org.team2471.frc.lib.framework.xPress

val primaryController by lazy { XboxController(Operator.PRIMARY_CONTROLLER) }
val secondaryJoystick by lazy { Joystick(Operator.SECONDARY_STICK) }

val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 1.0)
val liftSpeedScalar get() = Preferences.getInstance().getDouble("lift_speed_scalar", 1.0)

fun initControls() {
    val logger = Logger("Input")

    primaryController.createMappings {
        rightBumperHold { Claw.release(true) }
        leftBumperHold { Manipulators.intakeCargo(2.0) }

        aPress { Drivetrain.alignWithVision(VisionSource.Cargo) }
        xPress { Drivetrain.alignWithVision(VisionSource.Cargo) }
    }

    // Secondary joystick mappings
    secondaryJoystick.createMappings {
        // Bucket
        buttonPress(2) { Bucket.open() }

        // Conveyor
        buttonHold(4) { Conveyor.run() }
        buttonHold(16) { Conveyor.run(invert = true) }

        // Lift
        buttonHold(1) { Lift.manualControl() }
        buttonPress(7) { Lift.elevateTo(LiftState.CARGO_SHIP) }
        buttonPress(8) { Lift.elevateTo(LiftState.LOW) }
        buttonPress(9) { Lift.elevateTo(LiftState.MIDDLE) }
        buttonPress(10) { Lift.elevateTo(LiftState.HIGH) }
    }

    for (i in 0 until DriverStation.kJoystickPorts) {
        logger.addValue("Controller $i Type",
                        DriverStation.getInstance().getJoystickName(i) ?: "")
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
