package org.sert2521.deepspace.util

import edu.wpi.first.wpilibj.SendableBase
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder

class TextBox(
    text: Any = "",
    textColor: String = "ffffff",
    backgroundColor: String = "000000"
) : SendableBase() {
    private var textSupplier = { text.toString() }
    private var textColorSupplier = { textColor }
    private var backgroundColorSupplier = { backgroundColor }

    fun setText(text: String) {
        textSupplier = { text }
    }

    fun setText(text: () -> String) {
        textSupplier = text
    }

    fun setTextColor(textColor: String) {
        textColorSupplier = { textColor }
    }

    fun setTextColor(textColor: () -> String) {
        textColorSupplier = textColor
    }

    fun setBackgroundColor(backgroundColor: String) {
        backgroundColorSupplier = { backgroundColor }
    }

    fun setBackgroundColor(backgroundColor: () -> String) {
        backgroundColorSupplier = backgroundColor
    }

    override fun initSendable(builder: SendableBuilder) {
        builder.setSmartDashboardType("TextBox")
        builder.addStringProperty("Text", { textSupplier() }, null)
        builder.addStringProperty("TextColor", { textColorSupplier() }, null)
        builder.addStringProperty("BackgroundColor", { backgroundColorSupplier() }, null)
    }
}
