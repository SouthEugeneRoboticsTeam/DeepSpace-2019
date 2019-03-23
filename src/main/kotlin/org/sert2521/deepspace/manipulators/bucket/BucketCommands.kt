package org.sert2521.deepspace.manipulators.bucket

import org.team2471.frc.lib.framework.use

suspend fun Bucket.open() = use(this, name = "Open Bucket") {
    state = BucketState.OPEN
}

suspend fun Bucket.close() = use(this, name = "Close Bucket") {
    state = BucketState.CLOSED
}
