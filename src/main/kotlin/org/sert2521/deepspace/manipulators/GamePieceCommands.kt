package org.sert2521.deepspace.manipulators

import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.team2471.frc.lib.framework.use

suspend fun GamePiece.releaseCurrent() = use(GamePiece) {
    when (currentGamePiece) {
        GamePiece.GamePiece.CARGO -> Bucket.open(true)
        else -> Claw.release(true)
    }
}
