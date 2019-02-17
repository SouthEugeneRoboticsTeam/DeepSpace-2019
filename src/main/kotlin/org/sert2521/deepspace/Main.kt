package org.sert2521.deepspace

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlinx.coroutines.delay
import org.sert2521.deepspace.autonomous.AutoLoader
import org.sert2521.deepspace.autonomous.AutoMode
import org.sert2521.deepspace.drivetrain.Drivetrain
import org.sert2521.deepspace.lift.Lift
import org.sert2521.deepspace.manipulators.Manipulators
import org.sert2521.deepspace.manipulators.bucket.Bucket
import org.sert2521.deepspace.manipulators.claw.Claw
import org.sert2521.deepspace.manipulators.conveyor.Conveyor
import org.sert2521.deepspace.manipulators.intake.Intake
import org.sert2521.deepspace.util.Vision
import org.sert2521.deepspace.util.VisionSource
import org.sert2521.deepspace.util.initControls
import org.sert2521.deepspace.util.initLogs
import org.sert2521.deepspace.util.initPreferences
import org.sert2521.deepspace.util.logBuildInfo
import org.sert2521.deepspace.util.logger
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram

object Robot : RobotProgram {
    private val subsystems by lazy {
        arrayOf(Drivetrain, Lift, Intake, Conveyor, Claw, Bucket)
    }

    private val vision = Vision.getFromSource(VisionSource.Cargo)

    init {
        logger

        // Init subsystems
        subsystems

        // Init companions
        AutoLoader
        Manipulators

        // Turn off light
        vision.locked = false

        initControls()
        initPreferences()
        logBuildInfo()
        initLogs()
    }

    override suspend fun enable() {
        subsystems.forEach { it.enable() }

        Drivetrain.brake()
    }

    override suspend fun disable() {
        subsystems.forEach { it.disable() }

        suspendUntil { Math.abs(Drivetrain.speed) < 0.25 }
        Drivetrain.coast()
    }

    override suspend fun teleop() {
        println("Entering teleop...")
        Shuffleboard.selectTab("Driver")
    }

    override suspend fun autonomous() {
        delay(50)

        println("Entering autonomous...")
        Shuffleboard.selectTab("Autonomous")

        AutoMode.runAuto()
    }
}

fun main() {
    initializeWpilib()

    runRobotProgram(Robot)
}
