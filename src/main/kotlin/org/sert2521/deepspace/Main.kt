package org.sert2521.deepspace

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sert2521.deepspace.autonomous.AutoChooser
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.intake.Intake
import org.sert2521.deepspace.util.TelemetryScope
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

private var loggerJob: Job? = null
private val ds = DriverStation.getInstance()

object Robot : RobotProgram {
    private val subsystems by lazy { arrayOf(Drivetrain, Intake) }

    init {
        logger

        // Init subsystems
        subsystems

        // Init companions
        AutoChooser

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

        suspendUntil { Math.abs(Drivetrain.speed) < 0.25 }
        Drivetrain.coast()
    }

    override suspend fun teleop() {
        println("Entering teleop...")
        Shuffleboard.selectTab("Driver")
    }

    override suspend fun autonomous() {
        delay(100)

        println("Entering autonomous...")
        Shuffleboard.selectTab("Autonomous")

        AutoChooser.runAuto()
    }
}

private fun startLogger(interval: Double = if (ds.isEnabled) 0.1 else 0.25) {
    loggerJob?.cancel()
    loggerJob = TelemetryScope.launch {
        periodic(interval, watchOverrun = false) { log() }
    }
}

fun main() {
    initializeWpilib()

    runRobotProgram(Robot)
}
