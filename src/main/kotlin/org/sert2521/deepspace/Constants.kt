package org.sert2521.deepspace

object Talons {
    // Drivetrain
    const val DRIVE_LEFT_FRONT = 11
    const val DRIVE_LEFT_REAR = 12
    const val DRIVE_RIGHT_FRONT = 13
    const val DRIVE_RIGHT_REAR = 14

    // Intake
    const val INTAKE_ROLLER = -1
}

object Pneumatics {
    const val KICKER_RAISE_CHANNEL = -1
    const val KICKER_LOWER_CHANNEL = -1
    const val CARGOHOLD_RAISE_CHANNEL = -1
    const val CARGOHOLD_LOWER_CHANNEL = -1
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
