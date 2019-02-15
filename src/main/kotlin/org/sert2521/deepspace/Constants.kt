package org.sert2521.deepspace

import org.team2471.frc.lib.actuators.TalonID
import org.team2471.frc.lib.actuators.VictorID

object MotorControllers {
    // Drivetrain
    val DRIVE_RIGHT_FRONT = TalonID(11)
    val DRIVE_RIGHT_REAR = TalonID(12)
    val DRIVE_LEFT_FRONT = TalonID(13)
    val DRIVE_LEFT_REAR = TalonID(14)

    // Lift
    val LIFT_RIGHT = TalonID(15)
    val LIFT_LEFT = TalonID(16)

    // Climber
    val CLIMBER_RIGHT_REAR = TalonID(17)
    val CLIMBER_LEFT_REAR = TalonID(18)
    val CLIMBER_FRONT = VictorID(19)
    val CLIMBER_DRIVE = TalonID(20)

    // Conveyor
    val CONVEYOR_RIGHT = VictorID(21)
    val CONVEYOR_LEFT = VictorID(22)

    // Intake
    val INTAKE_RIGHT = VictorID(23)
    val INTAKE_LEFT = VictorID(24)
}

object Sensors {
    // Lift
    const val LIFT_SWITCH_TOP = 9
    const val LIFT_SWITCH_BOTTOM = 5

    // Climber
    const val CLIMBER_FRONT_POT = -1
    const val CLIMBER_REAR_POT = -1
    const val CLIMBER_LIDAR = -1

    // Claw
    const val CLAW_SWITCH = 2

    // Conveyor
    const val CONVEYOR_SWITCH = 0
}

object Pneumatics {
    // Claw
    const val CLAW_RELEASE = 1
    const val CLAW_CLAMP = 2

    // Cargo
    const val CARGO_RAISE = 3
    const val CARGO_LOWER = 7

    // Intake
    const val INTAKE_LOWER = 5
    const val INTAKE_RAISE = 6
}

object Operator {
    const val PRIMARY_STICK = 0
    const val SECONDARY_STICK = 1
    const val PRIMARY_CONTROLLER = 2

    const val TARGET_LEDS = 4
}

object Characteristics {
    const val WHEEL_DIAMETER = 6
    const val WHEELBASE_WIDTH = 22.0 / 12.0
    const val ENCODER_TICKS_PER_REVOLUTION = 4096
}
