package org.sert2521.deepspace.manipulators

import edu.wpi.first.wpilibj.InterruptHandlerFunction
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.BucketState
import org.sert2521.deepspace.util.Logger
import org.sert2521.deepspace.util.Telemetry
import org.sertain.hardware.DigitalInput

enum class GamePiece {
    CARGO, HATCH_PANEL
}

object Manipulators {
    private val telemetry = Telemetry("Manipulators")
    private val logger = Logger("Manipulators")

    private val hatchPanelSwitch = DigitalInput(Sensors.CLAW_SWITCH).invert()
    private val conveyorSwitch = DigitalInput(Sensors.CONVEYOR_SWITCH).invert()

    val hasCargoInConveyor get() = conveyorSwitch.get()
    val hasHatchPanel get() = hatchPanelSwitch.get()

    var hasCargo = false

    val currentGamePiece
        get() = when {
            hasHatchPanel -> GamePiece.HATCH_PANEL
            hasCargo -> GamePiece.CARGO
            else -> null
        }

    init {
        telemetry.add("Has Cargo in Conveyor") { hasCargoInConveyor }
        telemetry.add("Has Cargo") { hasCargo }
        telemetry.add("Has Hatch Panel") { hasHatchPanel }
        telemetry.add("Current Game Piece") { currentGamePiece?.name ?: "None" }

        logger.addBooleanTopic("Has Cargo in Conveyor") { hasCargoInConveyor }
        logger.addBooleanTopic("Has Cargo") { hasCargo }
        logger.addBooleanTopic("Has Hatch Panel") { hasHatchPanel }
        logger.addTopic("Current Game Piece") { currentGamePiece?.name ?: "None" }

        conveyorSwitch.requestInterrupts(object : InterruptHandlerFunction<Boolean>() {
            override fun interruptFired(interruptAssertedMask: Int, param: Boolean?) {
                hasCargo = true
                Bucket.state = BucketState.CLOSED
            }
        })

        conveyorSwitch.setUpSourceEdge(false, true)
        conveyorSwitch.enableInterrupts()
    }
}
