package org.sert2521.deepspace.util

import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem

private val instances = mutableListOf<Telemetry>()
private var telemetryJob: Job? = null

val GlobalTelemetry = Telemetry("Global")

data class Status(val state: State, val text: String? = null) {
    open class State(val name: String, val color: String, val textColor: String = "000000")
    object Nominal : State("Nominal", "00ff00")
    object Warning : State("Warning", "ffd700")
    object Error : State("Error", "ff0000")
}

class Telemetry {
    private data class Binding(val name: String, val body: () -> Any)

    private val name: String
    val table: NetworkTable

    private val statusBox = TextBox()
        get() {
            // Only publish status box when it is first accessed
            SmartDashboard.putData("$name Status", field)
            return field
        }

    private val bindings = mutableListOf<Binding>()

    constructor(name: String) {
        this.name = name
        table = NetworkTableInstance.getDefault().getTable(name)!!
    }

    constructor(subsystem: Subsystem) {
        name = subsystem.name
        table = NetworkTableInstance.getDefault().getTable(subsystem.name)!!
    }

    fun tick() = bindings.toList().forEach { put(it.name, it.body()) }

    fun add(name: String, body: () -> Any) = bindings.add(Binding(name, body))

    fun remove(name: String) = bindings.removeIf { it.name == name }

    fun put(name: String, value: Any) = table.getEntry(name).setValue(value)

    fun setStatus(status: Status) {
        statusBox.setText((status.text ?: "$name ${status.state.name}").toUpperCase())
        statusBox.setBackgroundColor(status.state.color)
        statusBox.setTextColor(status.state.textColor)
    }

    fun setStatus(body: () -> Status) {
        statusBox.setText { (body().text ?: "$name ${body().state.name}").toUpperCase() }
        statusBox.setBackgroundColor { body().state.color }
        statusBox.setTextColor { body().state.textColor }
    }

    init {
        instances += this

        launchTelemetry()
    }
}

fun launchTelemetry() {
    if (telemetryJob == null) {
        telemetryJob = GlobalScope.launch(MeanlibDispatcher) {
            periodic(0.1, false) {
                log()

                instances.toList().forEach {
                    it.tick()
                }

                SmartDashboard.updateValues()
            }
        }
    }
}
