package org.sert2521.deepspace.climber

import kotlinx.coroutines.GlobalScope
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun Climber.driveUntilAbovePlatform() = use(this) {
    periodic {
        if (elevated) drive(Climber.DriveDirection.FORWARD) else this@periodic.stop()
    }
    stop()
}

suspend fun Climber.elevateTo(position: Int) = use(this) {
    ensureSynced(Climber.SyncDirection.FRONT_TO_REAR)
    GlobalScope.parallel(
        { elevateFrontTo(position) },
        { elevateRearTo(position) }
    )
    ensureSynced(Climber.SyncDirection.FRONT_TO_REAR)
}

suspend fun Climber.elevateToTop() = elevateTo(CLIMBER_TOP_SENSOR_POSITION)
