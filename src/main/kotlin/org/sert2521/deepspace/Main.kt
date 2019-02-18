package org.sert2521.deepspace

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sert2521.deepspace.autonomous.AutoChooser
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.drivetrain.alignWithVision
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.lights.Lights.runLights
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.intake.Intake
import org.sert2521.deepspace.util.TelemetryScope
import org.sert2521.deepspace.util.Vision
import org.sert2521.deepspace.util.VisionSource
import org.sert2521.deepspace.util.initControls
import org.sert2521.deepspace.util.initLogs
import org.sert2521.deepspace.util.initPreferences
import org.sert2521.deepspace.util.log
import org.sert2521.deepspace.util.logBuildInfo
import org.sert2521.deepspace.util.logger
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram

private var loggerJob: Job? = null
private val ds = DriverStation.getInstance()

object Robot : RobotProgram {
    private val subsystems by lazy {
        arrayOf(Drivetrain, Lift, Intake, Conveyor, Claw, Bucket)
    }

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

        GlobalScope.launch(MeanlibDispatcher) {
            periodic(2.0) {
                Vision.light = !Vision.light
            }
        }
    }

    override suspend fun enable() {
        startLogger()

        subsystems.forEach { it.enable() }

        Drivetrain.brake()

        runLights()
    }

    override suspend fun disable() {
        startLogger()

        subsystems.forEach { it.disable() }

        suspendUntil { Math.abs(Drivetrain.speed) < 0.25 }
        Drivetrain.coast()

        val vision = Vision.getFromSource(VisionSource.Cargo)
        periodic(1.0) {
            val pose = vision.pose
            println("X: ${pose.xDistance}, Y: ${pose.yDistance}, Target Angle: ${pose.targetAngle}, Robot Angle: ${pose.robotAngle}")
        }
    }

    override suspend fun teleop() {
        println("Entering teleop...")
        Shuffleboard.selectTab("Driver")
    }

    override suspend fun autonomous() {
        delay(50)

        println("Entering autonomous...")
        Shuffleboard.selectTab("Autonomous")

//        AutoChooser.runAuto()
        Drivetrain.alignWithVision(VisionSource.Cargo)
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
