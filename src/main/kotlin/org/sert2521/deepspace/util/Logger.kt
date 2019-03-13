package org.sert2521.deepspace.util

import badlog.lib.BadLog
import badlog.lib.DataInferMode
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.RobotController
import org.sert2521.deepspace.Robot
import org.team2471.frc.lib.framework.Subsystem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.function.Supplier

private var startTime = 0.0
private val pathPrefix get() = if (RobotBase.isReal()) "/media/sda1" else "."

val logger by lazy {
    val path = File(pathPrefix)
    if (path.exists() && path.canWrite()) {
        BadLog.init("$pathPrefix/${System.currentTimeMillis()}.bag")
    } else {
        BadLog.init("/home/lvuser/${System.currentTimeMillis()}.bag")
    }!!
}

private val dateFormat = SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)

class Logger {
    private val subsystemName: String

    constructor(name: String) {
        subsystemName = name
    }

    constructor(subsystem: Subsystem) {
        subsystemName = subsystem.name
    }

    /**
     * Creates a named topic that logs a Double. Non-Double Numbers returned from the body will be
     * converted to Double before being logged.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param body the function to be called to return the logged data
     * @see BadLog.createTopicStr
     */
    fun addNumberTopic(name: String, unit: String = BadLog.UNITLESS, body: () -> Number) =
        BadLog.createTopic("$subsystemName/$name", unit, Supplier { body().toDouble() })

    /**
     * Creates a named topic that logs a Double. Non-Double Numbers returned from the body will be
     * converted to Double before being logged.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param body the function to be called to return the logged data
     * @param attrs array of topic attributes
     * @see BadLog.createTopicStr
     */
    fun addNumberTopic(
        name: String,
        unit: String = BadLog.UNITLESS,
        vararg attrs: String,
        body: () -> Number
    ) = BadLog.createTopic("$subsystemName/$name", unit, Supplier { body().toDouble() }, *attrs)

    /**
     * Creates a named topic that logs a Boolean.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param body the function to be called to return the logged data
     * @see BadLog.createTopicStr
     */
    fun addBooleanTopic(name: String, unit: String = BadLog.UNITLESS, body: () -> Boolean) =
        BadLog.createTopic("$subsystemName/$name", unit, Supplier { if (body()) 1.0 else 0.0 })

    /**
     * Creates a named topic that logs a Boolean.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param body the function to be called to return the logged data
     * @param attrs array of topic attributes
     * @see BadLog.createTopicStr
     */
    fun addBooleanTopic(
        name: String,
        unit: String = BadLog.UNITLESS,
        vararg attrs: String,
        body: () -> Boolean
    ) = BadLog.createTopic(
        "$subsystemName/$name",
        unit,
        Supplier { if (body()) 1.0 else 0.0 },
        *attrs
    )

    /**
     * Creates a named topic that logs a String. Non-String values returned from the body will be
     * converted to String before being logged.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param body the function to be called to return the logged data
     * @see BadLog.createTopicStr
     */
    fun addTopic(name: String, unit: String = BadLog.UNITLESS, body: () -> Any) =
        BadLog.createTopicStr("$subsystemName/$name", unit, Supplier { body().toString() })

    /**
     * Creates a named topic that logs a String. Non-String values returned from the body will be
     * converted to String before being logged.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param body the function to be called to return the logged data
     * @param attrs array of topic attributes
     * @see BadLog.createTopicStr
     */
    fun addTopic(
        name: String,
        unit: String = BadLog.UNITLESS,
        vararg attrs: String,
        body: () -> Any
    ) = BadLog.createTopicStr(
        "$subsystemName/$name",
        unit,
        Supplier { body().toString() },
        *attrs
    )

    /**
     * Creates a named value with a single value.
     *
     * @param name name of the value
     * @param value content to add
     * @see BadLog.createValue
     */
    fun addValue(name: String, value: Any) =
        BadLog.createValue("$subsystemName/$name", value.toString())

    /**
     * Creates a subscriber with the given name.
     *
     * @param name name of the topic
     * @param unit unit to assign values in this topic
     * @param inferMode method to use if data has not been published
     * @param attrs array of topic attributes
     * @see BadLog.createTopicSubscriber
     * @see publish
     */
    fun addSubscriber(
        name: String,
        unit: String = BadLog.UNITLESS,
        inferMode: DataInferMode = DataInferMode.DEFAULT,
        vararg attrs: String
    ) = BadLog.createTopicSubscriber("$subsystemName/$name", unit, inferMode, *attrs)

    /**
     * Publishes a value to the subscriber with the given name.
     *
     * @param name name of the topic
     * @param value new value to assign the topic
     * @see BadLog.publish
     * @see addSubscriber
     */
    fun publish(name: String, value: Any) =
        BadLog.publish("$subsystemName/$name", value.toString())
}

val SystemLogger = Logger("System")

fun log() {
    try {
        BadLog.publish("Time", (System.nanoTime().toDouble() - startTime) / 1000000000.0)

        logger.updateTopics()
        logger.log()
    } catch (exception: Exception) {
        // Logger could not update... Do nothing.
    }
}

fun initLogs() {
    startTime = System.nanoTime().toDouble()

    val ds = DriverStation.getInstance()

    BadLog.createValue("Start Time", dateFormat.format(Date()))
    BadLog.createValue("Event Name", ds.eventName)
    BadLog.createValue("Match Type", ds.matchType.toString())
    BadLog.createValue("Match Number", ds.matchNumber.toString())
    BadLog.createValue("Alliance", ds.alliance.toString())
    BadLog.createValue("Location", ds.location.toString())

    BadLog.createTopic("Match Time", "s", Supplier { DriverStation.getInstance().matchTime })

    SystemLogger.addNumberTopic("Battery Voltage", "V") { RobotController.getBatteryVoltage() }
    SystemLogger.addBooleanTopic("Browned Out") { RobotController.isBrownedOut() }
    SystemLogger.addBooleanTopic("FPGA Active") { RobotController.isSysActive() }
    SystemLogger.addBooleanTopic("DS Connected") { ds.isDSAttached }
    SystemLogger.addBooleanTopic("FMS Connected") { ds.isFMSAttached }

    BadLog.createTopicSubscriber("Time", "s", DataInferMode.DEFAULT, "hide", "delta", "xaxis")

    logControls()

    try {
        logger.finishInitialization()
    } catch (exception: Exception) {
        println("Error finishing logger initialization!")
        println(exception.toString())
    }
}

fun logControls() {
    val inputLogger = Logger("Input")
    val ds = DriverStation.getInstance()

    (0..(DriverStation.kJoystickPorts - 1)).forEach {
        inputLogger.addValue("Controller $it", ds.getJoystickName(it) ?: "")
    }
}

fun logBuildInfo() {
    println("\n-------------------- BUILD INFO --------------------")

    "branch.txt".asResource {
        println("Branch: $it")
        GlobalTelemetry.put("Branch", it)
        BadLog.createValue("Branch", it)
    }

    "commit.txt".asResource {
        println("Commit: $it")
        GlobalTelemetry.put("Commit", it)
        BadLog.createValue("Commit", it)
    }

    "changes.txt".asResource {
        println("Changes: [$it]")
        GlobalTelemetry.put("Changes", it)
        BadLog.createValue("Changes", it)
    }

    "buildtime.txt".asResource {
        val date = dateFormat.format(
            SimpleDateFormat("dd'-'MM'-'yyyy' 'HH:mm:ss", Locale.US).parse(it)
        )

        println("Build Time: $date")
        GlobalTelemetry.put("Build Time", date)
        BadLog.createValue("Build Time", date)
    }

    println("----------------------------------------------------\n")
}

fun String.asResource(body: (String) -> Unit) {
    val content = Robot.javaClass.getResource("/$this")?.readText()
    body(content ?: "")
}
