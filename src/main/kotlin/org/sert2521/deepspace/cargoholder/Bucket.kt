package org.sert2521.deepspace.cargoholder

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.team2471.frc.lib.framework.Subsystem


object Bucket : Subsystem("Bucket") {
    private enum class BucketState {
        OPEN, BLOCKED
    }

    private val solenoid = DoubleSolenoid(Pneumatics.CARGOHOLD_RAISE_CHANNEL, Pneumatics.CARGOHOLD_LOWER_CHANNEL)

    private var state = BucketState.BLOCKED
        set (value) {
            if (field == value) return

            when (value) {
                BucketState.BLOCKED -> DoubleSolenoid.Value.kForward
                BucketState.OPEN -> DoubleSolenoid.Value.kReverse
            }
            field = value
        }
    fun holdBall() {
        state = BucketState.BLOCKED
    }
    fun releaseBall() {
        state = BucketState.OPEN
    }
}