package org.sert2521.deepspace.manipulators.conveyor

import org.sert2521.deepspace.MotorControllers
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.framework.Subsystem

object Conveyor : Subsystem("Conveyor") {
    private val motor = MotorController(
        MotorControllers.CONVEYOR_RIGHT,
        MotorControllers.CONVEYOR_LEFT
    ).config {
        ctreFollowers.forEach { it.inverted = true }
    }

    fun setPercent(percent: Double = CONVEYOR_SPEED) = motor.setPercentOutput(percent)

    fun stop() = motor.stop()
}
