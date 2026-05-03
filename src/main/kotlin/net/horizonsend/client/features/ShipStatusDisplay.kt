package net.horizonsend.client.features

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.horizonsend.client.networking.packets.ShipData
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import java.awt.Color

object ShipStatusDisplay {
    fun init() {
        HudRenderCallback.EVENT.register { context, renderTickCounter ->
            println("isPiloting: ${ShipData.isPiloting}")
            if (!ShipData.isPiloting) return@register
            renderHud(context)
        }
    }

    private fun renderHud(context: DrawContext) {
        val x = 10
        val y = 5
        val renderer = MinecraftClient.getInstance().textRenderer
        val data = mutableMapOf<String, Any>(
            "Gravity Well" to if (ShipData.gravwell) "§aON" else "§cOFF",
            "Shield Regen Efficiency" to ShipData.regenEfficiency,
            "Weapon Set" to ShipData.weaponset,
            "Cruise" to "${ShipData.speed}/${ShipData.targetSpeed}",
            "Hull" to ShipData.hull
        )

        var longestText = 0
        var starting = 0

        fun increment() {
            starting += if (starting == 0) renderer.fontHeight else 5 + renderer.fontHeight
        }

        fun drawText(s: String) {
            increment()
            val textSize = renderer.getWidth(s)
            if (longestText < textSize) longestText = textSize
            context.drawText(renderer, s, x, y + starting, Color.WHITE.rgb, true)
        }

        drawText("Name: ")
        context.drawText(renderer, ShipData.name.string, x + renderer.getWidth("Name: "), y + starting, Color.WHITE.rgb, true)

        drawText("Type: ")
        context.drawText(renderer, ShipData.type.string, x + renderer.getWidth("Type: "), y + starting, Color.WHITE.rgb, true)

        for ((name, value) in data) {
            drawText("$name: $value")
        }

        increment()
        drawText("Turret Targets")
        for ((name, target) in ShipData.targets) {
            drawText(" • $name: $target")
        }

        increment()
        drawText("Power Modes")
        val sum = ShipData.shieldPowerMode + ShipData.weaponPowerMode + ShipData.thrusterPowerMode
        drawText(" • Shield: ${(ShipData.shieldPowerMode / sum * 100.0).toInt()}")
        drawText(" • Weapon: ${(ShipData.weaponPowerMode / sum * 100.0).toInt()}")
        drawText(" • Thruster: ${(ShipData.thrusterPowerMode / sum * 100.0).toInt()}")

        val x1 = x - 5
        val y1 = y
        val x2 = x + longestText + 5
        val y2 = y + starting + 2
        context.drawBorder(x1, y1, x2 - x1, y2 - y1, chromaColor().rgb)
    }

    fun chromaColor() =
        Color.getHSBColor((System.currentTimeMillis() % 2000f) / 1000f, 0.8f, 0.8f)
}