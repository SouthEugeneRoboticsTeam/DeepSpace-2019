package org.sert2521.deepspace.manipulators.bucket

import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.use

suspend fun Bucket.open(suspend: Boolean = false) = use(this) {
    state = Bucket.BucketState.OPEN

    if (suspend) {
        suspendUntil { false }
    }
}
