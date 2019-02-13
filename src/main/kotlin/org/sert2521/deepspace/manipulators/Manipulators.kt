package org.sert2521.deepspace.manipulators

import edu.wpi.first.wpilibj.InterruptHandlerFunction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.close
import org.sert2521.deepspace.util.Telemetry
import org.sertain.hardware.DigitalInput
import org.team2471.frc.lib.coroutines.MeanlibDispatcher

enum class GamePiece {
    CARGO, HATCH_PANEL
}

object Manipulators {
    val hatchPanelSwitch = DigitalInput(Sensors.CLAW_SWITCH).invert()
    val conveyorSwitch = DigitalInput(Sensors.CONVEYOR_SWITCH).invert()

    val hasCargoInConveyor get() = conveyorSwitch.get()
    val hasHatchPanel get() = hatchPanelSwitch.get()

    var hasCargo = false

    internal val currentGamePiece
        get() = when {
            hasHatchPanel -> GamePiece.HATCH_PANEL
            hasCargo -> GamePiece.CARGO
            else -> null
        }

    val telemetry = Telemetry("Manipulators")

    init {
        telemetry.add("Conveyor Switch") { hasCargoInConveyor }
        telemetry.add("Hatch Panel Switch") { hasHatchPanel }

        conveyorSwitch.requestInterrupts(object : InterruptHandlerFunction<Boolean>() {
            override fun interruptFired(interruptAssertedMask: Int, param: Boolean?) {
                hasCargo = true
                GlobalScope.launch(MeanlibDispatcher) {
                    Bucket.close(true)
                }
            }
        })

        conveyorSwitch.setUpSourceEdge(false, true)
        conveyorSwitch.enableInterrupts()
    }
}