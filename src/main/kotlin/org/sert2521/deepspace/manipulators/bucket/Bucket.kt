package org.sert2521.deepspace.manipulators.bucket

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.sert2521.deepspace.manipulators.Manipulators.hasCargo
import org.team2471.frc.lib.framework.Subsystem

object Bucket : Subsystem("Bucket") {
    internal enum class BucketState(val position: DoubleSolenoid.Value) {
        OPEN(DoubleSolenoid.Value.kReverse), CLOSED(DoubleSolenoid.Value.kForward)
    }

    private val solenoid = DoubleSolenoid(Pneumatics.CARGO_LOWER, Pneumatics.CARGO_RAISE).apply {
        set(DoubleSolenoid.Value.kReverse)
    }

    internal var state: BucketState = BucketState.OPEN
        set(value) {
            if (field != value) {
                field = value

                solenoid.set(value.position)

                if (value == Bucket.BucketState.OPEN) hasCargo = false
            }
        }

    init {
        state = Bucket.BucketState.OPEN
    }

    override suspend fun default() {
        state = BucketState.OPEN
    }
}
