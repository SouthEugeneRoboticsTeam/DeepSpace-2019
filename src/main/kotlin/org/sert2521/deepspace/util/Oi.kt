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
import org.sert2521.deepspace.manipulators.releaseCurrent
import org.team2471.frc.lib.framework.createMappings
import org.team2471.frc.lib.framework.leftBumperHold
import org.team2471.frc.lib.framework.rightBumperHold

val primaryController by lazy { XboxController(Operator.PRIMARY_CONTROLLER) }
val primaryJoystick by lazy { Joystick(Operator.PRIMARY_STICK) }
val secondaryJoystick by lazy { Joystick(Operator.SECONDARY_STICK) }

val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 1.0)
val liftSpeedScalar get() = Preferences.getInstance().getDouble("lift_speed_scalar", 1.0)

val driverIsJoystick
    get() = DriverStation.getInstance().getJoystickName(Operator.PRIMARY_STICK).isNotBlank()
val driverIsController
    get() = DriverStation.getInstance().getJoystickName(Operator.PRIMARY_CONTROLLER).isNotBlank()

fun initControls() {
    val logger = Logger("Input")

    primaryController.createMappings {
        rightBumperHold { Manipulators.releaseCurrent() }
        leftBumperHold { Manipulators.intakeCargo(2.0) }

        buttonPress(3) { Drivetrain.alignWithVision(VisionSource.Cargo) }
    }

    // Primary joystick mappings
    primaryJoystick.createMappings {
        buttonHold(1) { Manipulators.releaseCurrent() }
        buttonHold(3) { Manipulators.intakeCargo(2.0) }

        buttonPress(2) { Drivetrain.alignWithVision(VisionSource.Cargo) }
    }

    // Secondary joystick mappings
    secondaryJoystick.createMappings {
        buttonHold(1) { Lift.manualControl() }
        buttonHold(2) { Manipulators.intakeCargo(2.0) }
        buttonHold(3) { Conveyor.run() }
        buttonHold(4) { Conveyor.run(invert = true) }
        buttonHold(5) { Claw.release() }
        buttonPress(6) { Bucket.open() }
        buttonHold(7) { Manipulators.releaseCurrent() }

        buttonPress(8) { Lift.elevateTo(LiftState.LOW) }
        buttonPress(9) { Lift.elevateTo(LiftState.MIDDLE) }
        buttonPress(10) { Lift.elevateTo(LiftState.HIGH) }
    }

    for (i in 0 until DriverStation.kJoystickPorts) {
        logger.addValue("Controller $i Type",
                        DriverStation.getInstance().getJoystickName(i) ?: "Unknown")
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
