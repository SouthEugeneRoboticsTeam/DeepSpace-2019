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
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.conveyor.run
import org.sert2521.deepspace.manipulators.intake.Intake
import org.sert2521.deepspace.manipulators.intake.run
import org.sert2521.deepspace.manipulators.releaseCurrent
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.createMappings
import org.team2471.frc.lib.framework.rightBumperHold
import org.team2471.frc.lib.framework.xPress
import org.team2471.frc.lib.framework.yPress

val primaryJoystick by lazy { Joystick(Operator.PRIMARY_STICK) }
val secondaryJoystick by lazy { Joystick(Operator.SECONDARY_STICK) }

val primaryController by lazy { XboxController(0) }

val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 1.0)
val liftSpeedScalar get() = Preferences.getInstance().getDouble("lift_speed_scalar", 1.0)

fun initControls() {
    val logger = Logger("Input")

    primaryController.createMappings {
        xPress { Drivetrain.alignWithVision(VisionSource.Cargo, true) }
        yPress { Drivetrain.alignWithVision(VisionSource.Cargo) }
        rightBumperHold { Manipulators.releaseCurrent() }
    }

    // Primary joystick mappings
    primaryJoystick.createMappings {
        buttonHold(1) { Manipulators.releaseCurrent() }
    }

    // Secondary joystick mappings
    secondaryJoystick.createMappings {
        buttonHold(1) { Manipulators.releaseCurrent() }
        buttonHold(2) { parallel({ Conveyor.run(1.5) }, { Intake.run(1.0) }) }
        buttonHold(3) { Conveyor.run() }
        buttonHold(4) { Bucket.open(true) }

        buttonPress(8) { Lift.elevateTo(LiftState.HATCH_LOW) }
        buttonPress(9) { Lift.elevateTo(LiftState.HATCH_MIDDLE) }
        buttonPress(10) { Lift.elevateTo(LiftState.HATCH_HIGH) }
    }

    for (i in 0 until DriverStation.kJoystickPorts) {
        logger.addValue("Controller $i Type",
                        DriverStation.getInstance().getJoystickName(i) ?: "Unknown")
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
