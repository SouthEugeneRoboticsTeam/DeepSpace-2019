package org.sert2521.deepspace

object Talons {
    // Drivetrain
    const val DRIVE_RIGHT_FRONT = 11
    const val DRIVE_RIGHT_REAR = 12
    const val DRIVE_LEFT_FRONT = 13
    const val DRIVE_LEFT_REAR = 14

    // Lift
    const val LIFT_LEFT = 20
    const val LIFT_RIGHT = 21

    // Intake
    const val INTAKE_ROLLER = 22
}

object Sensors {
    // Lift
    const val LIFT_SWITCH_TOP = 1
    const val LIFT_SWITCH_BOTTOM = 2

    // Claw
    const val CLAW_SWITCH = -1
}

object Pneumatics {
    // Intake
    const val INTAKE_RAISE = -1
    const val INTAKE_LOWER = -1

    // Claw
    const val CLAW_OPEN = -1
    const val CLAW_CLOSE = -1
}

object Operator {
    const val PRIMARY_STICK = 0
    const val SECONDARY_STICK = 1
}

object Characteristics {
    const val WHEEL_DIAMETER = 6
    const val WHEELBASE_WIDTH = 22.0 / 12.0
    const val ENCODER_TICKS_PER_REVOLUTION = 4096
}
