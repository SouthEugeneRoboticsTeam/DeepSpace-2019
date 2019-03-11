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

/**
 * General telemetry values which don't fit another category.
 */
val GlobalTelemetry = Telemetry("Global")

/**
 * The current status of a system, including optional additional [text].
 *
 * @param state the current state of the system
 * @param text optional additional information about the status
 */
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

    /**
     * Creates a new [Telemetry] with a specified [name].
     *
     * @param name the name of this [Telemetry]
     */
    constructor(name: String) {
        this.name = name
        table = NetworkTableInstance.getDefault().getTable(name)!!
    }

    /**
     * Creates a new [Telemetry] from a specified [subsystem].
     *
     * @param subsystem the [Subsystem] that this [Telemetry] belongs to
     */
    constructor(subsystem: Subsystem) {
        name = subsystem.name
        table = NetworkTableInstance.getDefault().getTable(subsystem.name)!!
    }

    /**
     * Publishes all values to NetworkTables.
     */
    fun tick() = bindings.toList().forEach { put(it.name, it.body()) }

    /**
     * Adds a new value to publish.
     *
     * @param name the name of the telemetry value
     * @param body a function which returns the telemetry value
     */
    fun add(name: String, body: () -> Any) = bindings.add(Binding(name, body))

    /**
     * Stops publishing a value.
     *
     * @param name the name of the telemetry value
     */
    fun remove(name: String) = bindings.removeIf { it.name == name }

    /**
     * Publishes a value to NetworkTables.
     *
     * @param key the key of the value to publish
     * @param value the value to publish
     */
    fun put(key: String, value: Any) = table.getEntry(key).setValue(value)

    /**
     * Sets the current system [Status].
     *
     * @param status the current system [Status]
     */
    fun setStatus(status: Status) {
        statusBox.setText((status.text ?: "$name ${status.state.name}").toUpperCase())
        statusBox.setBackgroundColor(status.state.color)
        statusBox.setTextColor(status.state.textColor)
    }

    /**
     * Sets the current system [Status].
     *
     * @param body a function that returns the system [Status]
     */
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

/**
 * Launches the telemetry coroutine if it is not already launched.
 */
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
