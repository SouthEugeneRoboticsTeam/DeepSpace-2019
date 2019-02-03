package org.sert2521.deepspace.util

import org.sert2521.deepspace.Characteristics
import java.lang.Math.PI
import java.util.stream.Collector

data class Unit<T : Unit.Type>(val type: T, val factor: Double) {
    sealed class Type {
        object Linear : Type()
        object Square : Type()
        object Cubic : Type()
        object Angular : Type()
    }
}

fun <T : Unit.Type> Int.convert(units: Pair<Unit<T>, Unit<T>>) =
    this * (units.first.factor / units.second.factor)

val m = Unit(Unit.Type.Linear, 1.0)
val inch = Unit(Unit.Type.Linear, 39.3701)
val et = Unit(Unit.Type.Linear, 26876.6549)

val rad = Unit(Unit.Type.Angular, PI)
val deg = Unit(Unit.Type.Angular, 180.0)
