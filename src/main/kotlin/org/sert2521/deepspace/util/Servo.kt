package org.sert2521.deepspace.util

import edu.wpi.first.wpilibj.Servo as WPI_Servo

class Servo(port: Int, vararg followerPorts: Int) : WPI_Servo(port) {
    private val maxServoAngle = 180.0
    private val minServoAngle = 0.0

    private var servos = followerPorts.map { WPI_Servo(it) }

    var invertPrimary = false
    var invertFollowers = false

    override fun set(value: Double) {
        super.set(if (invertPrimary) 1 - value else value)
        servos.forEach { it.set(if (invertFollowers) 1 - value else value) }
    }

    override fun setAngle(degrees: Double) {
        val value = when {
            degrees < minServoAngle -> minServoAngle
            degrees > maxServoAngle -> maxServoAngle
            else -> degrees
        }

        set((value - minServoAngle) / (maxServoAngle - minServoAngle))
    }
}
