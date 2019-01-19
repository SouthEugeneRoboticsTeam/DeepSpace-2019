package org.sert2521.deepspace

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlinx.coroutines.launch
import org.sert2521.deepspace.autonomous.AutoChooser
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.util.TelemetryScope
import org.sert2521.deepspace.util.Vision
import org.sert2521.deepspace.util.initControls
import org.sert2521.deepspace.util.initLogs
import org.sert2521.deepspace.util.initPreferences
import org.sert2521.deepspace.util.log
import org.sert2521.deepspace.util.logBuildInfo
import org.sert2521.deepspace.util.logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram

val subsystems = arrayOf(Drivetrain)

private var loggerJob: Job? = null
private val ds = DriverStation.getInstance()

object Robot : RobotProgram {
    init {
        logger

        initControls()
        initPreferences()
        logBuildInfo()
        initLogs()
    }

    override suspend fun enable() {
        startLogger()

        subsystems.forEach { it.enable() }

        Drivetrain.brake()
    }

    override suspend fun disable() {
        startLogger()

        subsystems.forEach { it.disable() }

        suspendUntil { Math.abs(Drivetrain.speed) < 0.5 }
        Drivetrain.coast()
    }

    override suspend fun teleop() {
        println("Entering teleop...")
        Shuffleboard.selectTab("Driver")
    }

    override suspend fun autonomous() {
        println("Entering autonomous...")
        Shuffleboard.selectTab("Autonomous")

        AutoChooser.runAuto()
    }
}

private fun startLogger(interval: Double = if (ds.isEnabled) 0.1 else 0.25) {
    loggerJob?.cancel()
    loggerJob = TelemetryScope.launch {
        periodic(interval) { log() }
    }
}

fun main() {
    initializeWpilib()

    AutoChooser
    Vision

    runRobotProgram(Robot)
}
