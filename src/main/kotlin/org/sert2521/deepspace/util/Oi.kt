package org.sert2521.deepspace.util

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.Preferences
import edu.wpi.first.wpilibj.XboxController
import org.sert2521.deepspace.Operator
import org.team2471.frc.lib.framework.createMappings

val primaryJoystick by lazy { Joystick(Operator.PRIMARY_STICK) }
val secondaryJoystick by lazy { Joystick(Operator.SECONDARY_STICK) }

val primaryController by lazy { XboxController(0) }

val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 1.0)

fun initControls() {
    val logger = Logger("Input")

    primaryJoystick.createMappings {
        // Primary joystick mappings
    }

    secondaryJoystick.createMappings {
        // Secondary joystick mappings
    }

    for (i in 0 until DriverStation.kJoystickPorts) {
        logger.addValue("Controller $i Type",
                        DriverStation.getInstance().getJoystickName(i) ?: "Unknown")
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
