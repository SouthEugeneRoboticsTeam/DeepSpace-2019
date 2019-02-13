package org.sert2521.deepspace.manipulators

import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.team2471.frc.lib.framework.use

suspend fun Manipulators.releaseCurrent() = when (currentGamePiece) {
    GamePiece.CARGO -> use(Bucket) { Bucket.open() }
    GamePiece.HATCH_PANEL -> use(Claw) { Claw.release(true) }
    else -> Unit
}
