package org.sert2521.deepspace.lights

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Servo
import org.sert2521.deepspace.Operator.STATUS_LEDS
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.util.Vision
import org.sert2521.deepspace.util.VisionSource

object Lights {

    private val statusLights  = Servo(STATUS_LEDS)

    fun colorMap(hue: Double) : Double {
            return (hue%360.0)/360.0*.8+.2
    }

    enum class LightMode(var value: Double) {
        RED(0.15), BLUE(0.05), TARGETING(colorMap(303.0)), HAS_HATCH(colorMap(62.0)),
        HAS_BALL(colorMap(25.0))
    }

    fun runLights() {
        if(Vision.getFromSource(VisionSource.Cargo).locked || Vision.getFromSource(VisionSource.HatchPanel).locked) {
            statusLights.set(LightMode.TARGETING.value)
        }
        else if(Manipulators.hasCargo){
            statusLights.set(LightMode.HAS_BALL.value)
        }
        else if(Manipulators.hasHatchPanel) {
            statusLights.set(LightMode.HAS_HATCH.value)
        }
        else if(DriverStation.getInstance().alliance == DriverStation.Alliance.Red){
            statusLights.set(LightMode.RED.value)
        }
        else{
            statusLights.set(LightMode.BLUE.value)
        }
    }
}
