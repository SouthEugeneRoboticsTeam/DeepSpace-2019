package org.sert2521.deepspace.climber

import edu.wpi.first.wpilibj.AnalogInput
import org.sert2521.deepspace.MotorControllers
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.util.tol
import org.team2471.frc.lib.actuators.MotorController
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem

object Climber : Subsystem("Climber") {
    enum class DriveDirection {
        FORWARD, BACKWARD
    }

    enum class SyncDirection {
        REAR_TO_FRONT, FRONT_TO_REAR
    }

    private val drive = MotorController(MotorControllers.CLIMBER_DRIVE)
    private val frontLegs = MotorController(MotorControllers.CLIMBER_FRONT)
    private val rearLegs = MotorController(
        MotorControllers.CLIMBER_LEFT_REAR,
        MotorControllers.CLIMBER_RIGHT_REAR
    )

    private val frontPot = AnalogInput(Sensors.CLIMBER_FRONT_POT)
    private val rearPot = AnalogInput(Sensors.CLIMBER_REAR_POT)
    private val lidar = AnalogInput(Sensors.CLIMBER_LIDAR)

    val frontLegPosition get() = frontPot.value
    val rearLegPosition get() = rearPot.value

    /**
     * Whether the robot is currently elevated.
     */
    val elevated get() = lidar.value > 1000

    /**
     * Whether the legs are within a specified tolerance.
     */
    val synced get() = frontLegPosition in (rearLegPosition tol ALLOWED_CLIMBER_ERROR)

    /**
     * Elevate front legs to absolute potentiometer position.
     *
     * @param position desired potentiometer position
     */
    suspend fun elevateFrontTo(position: Int) {
        periodic {
            if (frontLegPosition !in (position tol ALLOWED_CLIMBER_ERROR)) {
                frontLegs.setVelocitySetpoint(
                    if (frontLegPosition < position) CLIMBER_SPEED else -CLIMBER_SPEED
                )
            }
        }
    }

    /**
     * Elevate rear legs to absolute potentiometer position.
     *
     * @param position desired potentiometer position
     */
    suspend fun elevateRearTo(position: Int) {
        periodic {
            if (rearLegPosition !in (position tol ALLOWED_CLIMBER_ERROR)) {
                rearLegs.setVelocitySetpoint(
                    if (rearLegPosition < position) CLIMBER_SPEED else -CLIMBER_SPEED
                )
            }
        }
    }

    /**
     * Elevate front legs to relative potentiometer position.
     *
     * @param delta desired change in potentiometer position
     */
    suspend fun elevateFrontBy(delta: Int) = elevateFrontTo(frontLegPosition + delta)

    /**
     * Elevate rear legs to relative potentiometer position.
     *
     * @param delta desired change in potentiometer position
     */
    suspend fun elevateRearBy(delta: Int) = elevateRearTo(rearLegPosition + delta)

    /**
     * Sync front and rear legs, so that one moves to the position of the other.
     *
     * @param direction specify which leg should move to the other
     * @return whether the legs are now synced
     */
    suspend fun sync(direction: SyncDirection): Boolean {
        if (direction == Climber.SyncDirection.REAR_TO_FRONT) {
            elevateRearTo(frontLegPosition)
        } else {
            elevateFrontTo(rearLegPosition)
        }
        return synced
    }

    /**
     * Repeat [sync] until it returns true.
     */
    tailrec suspend fun ensureSynced(direction: SyncDirection) {
        if (sync(direction)) return
        ensureSynced(direction)
    }

    /**
     * Drive the climber at a constant speed in the desired [direction].
     *
     * @param direction direction to move to
     */
    fun drive(direction: DriveDirection) {
        drive.setVelocitySetpoint(
            if (direction == DriveDirection.FORWARD) CLIMBER_DRIVE_SPEED
            else -CLIMBER_DRIVE_SPEED
        )
    }

    /**
     * Stop all driving in the climber.
     */
    fun stop() = drive.stop()
}
