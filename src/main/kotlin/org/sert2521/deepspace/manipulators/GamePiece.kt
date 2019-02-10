package org.sert2521.deepspace.manipulators

import edu.wpi.first.wpilibj.InterruptHandlerFunction
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.util.Telemetry
import org.sertain.hardware.DigitalInput
import org.team2471.frc.lib.framework.Subsystem

object GamePiece : Subsystem("GamePiece") {
    internal enum class GamePiece {
        CARGO, HATCH_PANEL
    }

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

    val telemetry = Telemetry(this)

    init {
        telemetry.add("Conveyor Switch") { hasCargoInConveyor }
        telemetry.add("Hatch Panel Switch") { hasHatchPanel }

        conveyorSwitch.requestInterrupts(object : InterruptHandlerFunction<Boolean>() {
            override fun interruptFired(interruptAssertedMask: Int, param: Boolean?) {
                hasCargo = true
            }
        })

        conveyorSwitch.setUpSourceEdge(false, true)
        conveyorSwitch.enableInterrupts()
    }
}
