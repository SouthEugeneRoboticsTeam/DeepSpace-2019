package org.sert2521.deepspace.manipulators.conveyor

import org.sert2521.deepspace.MotorControllers
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem

object Conveyor : Subsystem("Conveyor") {
    val motor = MotorController(
        MotorControllers.CONVEYOR_RIGHT,
        MotorControllers.CONVEYOR_LEFT
    ).config {
        ctreFollowers.forEach { it.inverted = true }
    }

    fun runSpeed(speed: Double = CONVEYOR_SPEED) {
        motor.setPercentOutput(speed)
    }

    fun stop() = motor.stop()
}
