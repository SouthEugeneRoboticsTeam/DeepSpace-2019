package org.sert2521.deepspace.manipulators

import kotlinx.coroutines.GlobalScope
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.open
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.claw.release
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.conveyor.run
import org.sert2521.deepspace.manipulators.intake.Intake
import org.sert2521.deepspace.manipulators.intake.run
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.use

suspend fun Manipulators.releaseCurrent() = when (currentGamePiece) {
    GamePiece.CARGO -> use(Bucket) { Bucket.open() }
    GamePiece.HATCH_PANEL -> use(Claw) { Claw.release(true) }
    else -> Unit
}

suspend fun Manipulators.intakeCargo() =
    GlobalScope.parallel({ Conveyor.run(1.5) }, { Intake.run(1.0) })
