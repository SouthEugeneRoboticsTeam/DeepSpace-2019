package org.sert2521.deepspace.manipulators.bucket

import edu.wpi.first.wpilibj.DoubleSolenoid
import org.sert2521.deepspace.Pneumatics
import org.sert2521.deepspace.manipulators.Manipulators.hasCargo
import org.team2471.frc.lib.framework.Subsystem

enum class BucketState(val position: DoubleSolenoid.Value) {
    OPEN(DoubleSolenoid.Value.kReverse), CLOSED(DoubleSolenoid.Value.kForward)
}

object Bucket : Subsystem("Bucket") {
    private val solenoid = DoubleSolenoid(Pneumatics.CARGO_LOWER, Pneumatics.CARGO_RAISE)

    var state: BucketState = BucketState.OPEN
        set(value) {
            field = value

            solenoid.set(value.position)

            if (value == BucketState.OPEN) hasCargo = false
        }

    init {
        state = BucketState.OPEN
    }
}
