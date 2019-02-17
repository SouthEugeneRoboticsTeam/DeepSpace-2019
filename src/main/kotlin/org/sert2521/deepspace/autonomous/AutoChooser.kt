package org.sert2521.deepspace.autonomous

import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.EntryNotification
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.sertain.util.SendableChooser
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.util.measureTimeFPGA
import java.io.File

lateinit var autonomi: Autonomi

enum class AutoMode(val command: suspend () -> Unit) {
    CROSS_BASELINE({ crossBaseline() }),

    LEVEL_ONE_TO_ROCKET({ levelOneToRocket(start, constraint != Constraint.NO_PICKUP) }),
    LEVEL_ONE_TO_CARGO_SIDE({ levelOneToCargoSide(start, constraint != Constraint.NO_PICKUP) }),
    LEVEL_ONE_TO_CARGO_FRONT({ levelOneToCargoFront(start, constraint != Constraint.NO_PICKUP) }),

    LEVEL_TWO_TO_ROCKET({ }),
    LEVEL_TWO_TO_CARGO_SIDE({ });

    data class StartPosition(val location: Location, val level: Level)

    enum class Location {
        LEFT, MIDDLE, RIGHT;
    }

    enum class Level {
        ONE, TWO
    }

    enum class Objective {
        BASELINE, CARGO_FRONT, CARGO_SIDE, ROCKET_FRONT
    }

    enum class Constraint {
        NONE, NO_PICKUP
    }

    companion object {
        val startChooser = SendableChooser(
                "Middle (Level 1)" to StartPosition(Location.MIDDLE, Level.ONE),
                "Left (Level 1)" to StartPosition(Location.LEFT, Level.ONE),
                "Right (Level 1)" to StartPosition(Location.RIGHT, Level.ONE),
                "Left (Level 2)" to StartPosition(Location.LEFT, Level.TWO),
                "Right (Level 2)" to StartPosition(Location.RIGHT, Level.TWO)
        )
        val objectiveChooser = SendableChooser(
                "Baseline" to Objective.BASELINE,
                "Cargo Front" to Objective.CARGO_FRONT,
                "Cargo Side" to Objective.CARGO_SIDE,
                "Rocket Front" to Objective.ROCKET_FRONT
        )
        val constraintChooser = SendableChooser(
                "None" to Constraint.NONE,
                "No Pickup" to Constraint.NO_PICKUP
        )

        val start get() = startChooser.selected ?: StartPosition(Location.MIDDLE, Level.ONE)
        val objective get() = objectiveChooser.selected ?: Objective.BASELINE
        val constraint get() = constraintChooser.selected ?: Constraint.NONE

        private fun calculateAuto() = when (AutoMode.objective) {
            Objective.BASELINE -> AutoMode.CROSS_BASELINE
            Objective.CARGO_FRONT -> AutoMode.LEVEL_ONE_TO_CARGO_FRONT
            Objective.CARGO_SIDE -> when (AutoMode.start.level) {
                AutoMode.Level.ONE -> AutoMode.LEVEL_ONE_TO_CARGO_SIDE
                AutoMode.Level.TWO -> AutoMode.LEVEL_TWO_TO_CARGO_SIDE
            }
            Objective.ROCKET_FRONT -> when (AutoMode.start.level) {
                AutoMode.Level.ONE -> AutoMode.LEVEL_ONE_TO_ROCKET
                AutoMode.Level.TWO -> AutoMode.LEVEL_TWO_TO_CARGO_SIDE
            }
        }

        suspend fun runAuto() = AutoMode.calculateAuto().command()
    }
}

object AutoLoader {
    private val cacheFile = File("/home/lvuser/autonomi.json")

    init {
        SmartDashboard.putData("Auto Start Position", AutoMode.startChooser)
        SmartDashboard.putData("Auto Objective", AutoMode.objectiveChooser)
        SmartDashboard.putData("Auto Constraint", AutoMode.constraintChooser)

        try {
            autonomi = Autonomi.fromJsonString(cacheFile.readText())
            println("Autonomi cache loaded.")
        } catch (_: Exception) {
            DriverStation.reportError("Autonomi cache could not be found", false)
            autonomi = Autonomi()
        }

        val handler = { event: EntryNotification ->
            val json = event.value.string

            println("Received new autonomi JSON")

            if (!json.isEmpty()) {
                val t = measureTimeFPGA {
                    autonomi = Autonomi.fromJsonString(json)
                }

                println("Loaded autonomi in $t seconds")

                cacheFile.writeText(json)
                println("New autonomi written to cache")
            } else {
                autonomi = Autonomi()
                DriverStation.reportWarning("Empty autonomi received from NetworkTables", false)
            }
        }

        val flags = EntryListenerFlags.kImmediate or
                EntryListenerFlags.kNew or
                EntryListenerFlags.kUpdate

        NetworkTableInstance.getDefault()
                .getTable("PathVisualizer")
                .getEntry("Autonomi")
                .addListener(handler, flags)
    }
}
