package org.sert2521.deepspace.cargoholder

import org.sert2521.deepspace.intake.Intake
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runCargohold() = use(Bucket) {
    try {
        Bucket.releaseBall()
    }finally {
        Bucket.holdBall()
    }
}