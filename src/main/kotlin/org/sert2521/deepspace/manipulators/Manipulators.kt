package org.sert2521.deepspace.manipulators

import edu.wpi.first.wpilibj.Compressor
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.InterruptHandlerFunction
import org.sert2521.deepspace.Sensors
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.bucket.BucketState
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
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

    private val compressor = Compressor()

    val hasCargoInConveyor get() = conveyorSwitch.get()
    val hasHatchPanel get() = hatchPanelSwitch.get()

    var hasCargo = false

    val currentGamePiece
        get() = when {
            hasHatchPanel -> GamePiece.HATCH_PANEL
            hasCargo -> GamePiece.CARGO
            else -> null
        }

    var compressorEnabled = true
        set(value) {
            field = value

            if (value) {
                compressor.start()
            } else {
                compressor.stop()
            }
        }

    init {
        telemetry.add("Has Cargo in Conveyor") { hasCargoInConveyor }
        telemetry.add("Has Cargo") { hasCargo }
        telemetry.add("Has Hatch Panel") { hasHatchPanel }

        logger.addBooleanTopic("Has Cargo in Conveyor") { hasCargoInConveyor }
        logger.addBooleanTopic("Has Cargo") { hasCargo }
        logger.addBooleanTopic("Has Hatch Panel") { hasHatchPanel }

        conveyorSwitch.requestInterrupts(object : InterruptHandlerFunction<Boolean>() {
            override fun interruptFired(interruptAssertedMask: Int, param: Boolean?) {
                if (DriverStation.getInstance().isEnabled && Conveyor.isRunning) {
                    hasCargo = true
                    Bucket.state = BucketState.CLOSED
                }
            }
        })

        conveyorSwitch.setUpSourceEdge(false, true)
        conveyorSwitch.enableInterrupts()
    }
}
