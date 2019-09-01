package org.sert2521.deepspace.util

import edu.wpi.first.wpilibj.Preferences
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.sert2521.deepspace.Operator
import org.sert2521.deepspace.climber.Climber
import org.sert2521.deepspace.climber.ClimberState
import org.sert2521.deepspace.climber.runClimbSequence
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.alignWithVision
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.lift.LiftState
import org.sert2521.deepspace.lift.elevateTo
import org.sert2521.deepspace.lift.manualControl
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.close
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.conveyor.run
import org.sert2521.deepspace.manipulators.intakeCargo
import org.sertain.util.SendableChooser
import org.team2471.frc.lib.input.Joystick
import org.team2471.frc.lib.input.XboxController
import org.team2471.frc.lib.input.whenTrue
import org.team2471.frc.lib.input.whileTrue

enum class ControlMode {
    CONTROLLER, JOYSTICK
}

val primaryController by lazy { XboxController(Operator.PRIMARY_CONTROLLER) }
val primaryJoystick by lazy { Joystick(Operator.PRIMARY_STICK) }
val secondaryJoystick by lazy { Joystick(Operator.SECONDARY_STICK) }

val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 1.0)
val liftSpeedScalar get() = Preferences.getInstance().getDouble("lift_speed_scalar", 1.0)

private val controlModeChooser = SendableChooser(
    "Controller" to ControlMode.CONTROLLER,
    "Joystick" to ControlMode.JOYSTICK
)

val controlMode get() = controlModeChooser.selected ?: ControlMode.CONTROLLER

fun initControls() {
    SmartDashboard.putData("Control Mode", controlModeChooser)

    primaryController.run {
        // Manipulators
        ({ rightBumper }).whileTrue { Claw.release(true) }
        ({ leftBumper }).whileTrue { Manipulators.intakeCargo(2.0) }

        // Alignment
        ({ a }).whenTrue { Drivetrain.alignWithVision(VisionSource.Cargo, alignOnly = true) }

        ({ x }).whenTrue { setDriverCamera(DriverCameraSource.Down) }
        ({ y }).whenTrue { setDriverCamera(DriverCameraSource.Forward) }
    }

    primaryJoystick.run {
        // Manipulators
        ({ getButton(1) }).whileTrue { Claw.release(true) }
        ({ getButton(3) }).whileTrue { Manipulators.intakeCargo(2.0) }

        // Alignment
        ({ getButton(2) }).whenTrue { Drivetrain.alignWithVision(VisionSource.Cargo, alignOnly = true) }

        ({ getButton(7) }).whenTrue { setDriverCamera(DriverCameraSource.Down) }
        ({ getButton(8) }).whenTrue { setDriverCamera(DriverCameraSource.Forward) }
    }

    secondaryJoystick.run {
        // Bucket
        ({ getButton(2) }).whenTrue { Bucket.open() }
        ({ getButton(3) }).whenTrue { Bucket.close() }

        // Conveyor
        ({ getButton(4) }).whileTrue { Conveyor.run(override = true) }
        ({ getButton(16) }).whileTrue { Conveyor.run(invert = true, override = true) }

        // Lift
        ({ getButton(1) }).whileTrue { Lift.manualControl() }
        ({ getButton(7) }).whenTrue { Lift.elevateTo(LiftState.CARGO_SHIP) }
        ({ getButton(8) }).whenTrue { Lift.elevateTo(LiftState.LOW) }
        ({ getButton(9) }).whenTrue { Lift.elevateTo(LiftState.MIDDLE) }
        ({ getButton(10) }).whenTrue { Lift.elevateTo(LiftState.HIGH) }

        // Climber
        ({ getButton(11) }).whenTrue { Climber.abort() }
        ({ getButton(13) && getButton(14) }).whenTrue { Climber.runClimbSequence(ClimberState.LEVEL_2) }
        ({ getButton(13) && getButton(15) }).whenTrue { Climber.runClimbSequence(ClimberState.LEVEL_3) }
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
