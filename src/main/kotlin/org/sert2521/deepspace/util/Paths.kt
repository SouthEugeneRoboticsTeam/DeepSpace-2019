package org.sert2521.deepspace.util

import org.team2471.frc.lib.motion_profiling.Path2D

fun Path2D.addPointToEnd(
    x: Double,
    y: Double,
    angle: Double? = null,
    magnitude: Double? = null,
    xTangent: Double? = null,
    yTangent: Double? = null
) {
    // Ensure that one of the legal parameter configurations is specified
    val illegal = listOf(
        angle == null && magnitude == null && xTangent == null && yTangent == null,
        angle != null && magnitude != null,
        xTangent != null && yTangent != null
    ).singleOrNull { it } == null

    if (illegal) throw IllegalArgumentException("""
        One of [angle and magnitude], [xTangent and yTangent] or none must be specified. Specifying
        angle without magnitude or xTangent without yTangent is illegal.
    """.trimIndent().replace("\n", " "))

    if (angle != null && magnitude != null) {
        addPointAngleAndMagnitude(x, y, angle, magnitude)
    } else if (xTangent != null && yTangent != null) {
        addPointAndTangent(x, y, xTangent, yTangent)
    } else {
        addPoint(x, y)
    }
}

fun Path2D.addEasePointToEnd(
    time: Double,
    value: Double,
    slope: Double? = null,
    magnitude: Double? = null
) {
    // Ensure that one of the legal parameter configurations is specified
    // One must specify either a slope and magnitude or neither
    val illegal = listOf(
        slope == null && magnitude == null,
        slope != null && magnitude != null
    ).singleOrNull { it } == null

    if (illegal) throw IllegalArgumentException("""
        One of [slope and magnitude] or none must be specified. Specifying slope or magnitude
        without the other is illegal.
    """.trimIndent().replace("\n", " "))

    if (slope != null && magnitude != null) {
        addEasePointSlopeAndMagnitude(time, value, slope, magnitude)
    } else {
        addEasePoint(time, value)
    }
}

fun Path2D.updateEndpoint(
    x: Double,
    y: Double,
    angle: Double? = null,
    magnitude: Double? = null,
    xTangent: Double? = null,
    yTangent: Double? = null
) {
    println("OLD TAIL: ${xyCurve.tailPoint.position}, NEW TAIL: ($x, $y)")

    // Only remove endpoint if it's not the only point on the curve
    if (xyCurve.tailPoint != xyCurve.headPoint) {
        removePoint(xyCurve.tailPoint)
    }

    addPointToEnd(x, y, angle, magnitude, xTangent, yTangent)
}

fun Path2D.updateEaseEndpoint(
    time: Double,
    value: Double,
    slope: Double? = null,
    magnitude: Double? = null
) {
    // Only remove endpoint if it's not the only point on the curve
    if (easeCurve.tailKey != easeCurve.headKey) {
        easeCurve.removeKey(easeCurve.tailKey)
    }

    addEasePointToEnd(time, value, slope, magnitude)
}
