package net.horizonsend.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.horizonsend.client.features.CratePlacer
import net.horizonsend.client.features.ReiIntegration
import net.horizonsend.client.features.ShipStatusDisplay
import net.horizonsend.client.networking.IonPayload
import net.horizonsend.client.networking.Packets
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import kotlin.properties.Delegates

val mc get() = MinecraftClient.getInstance()

@Environment(EnvType.CLIENT)
@Suppress("Unused")
object Void : ClientModInitializer {
    var isCratePlacerActive: Boolean = false
    val cratePlacer = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "Activate Crate Placer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            "Void",
        )
    )
    var reiExists by Delegates.notNull<Boolean>()

    override fun onInitializeClient() {
        println("Void: Starting init")
        reiExists = FabricLoader.getInstance().isModLoaded("roughlyenoughitems")
        println("Void: REI exists: $reiExists")

        for (packet in Packets.values()) {
            println("Void: Registering packet ${packet.handler.name}")
            PayloadTypeRegistry.playS2C().register(
                packet.handler.payloadId,
                IonPayload.codec(packet.handler)
            )
            println("Void: Registered payload type for ${packet.handler.name}")
            ClientPlayNetworking.registerGlobalReceiver(packet.handler.payloadId) { payload, context ->
                packet.handler.s2c(
                    context.client(),
                    context.player().networkHandler,
                    payload.buf,
                    context.responseSender()
                )
            }
            println("Void: Registered receiver for ${packet.handler.name}")
        }

        println("Void: Initializing ShipStatusDisplay")
        ShipStatusDisplay.init()
        println("Void: Registering disconnect handler")
        DISCONNECT.register { _, _ -> ReiIntegration.items.clear() }
        println("Void: Registering tick handler")
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            handleKeybinds()
            CratePlacer.handleCratePlacer()
        }
        println("Void: Init complete")
    }

    private fun handleKeybinds() {
        if (cratePlacer.wasPressed()) {
            isCratePlacerActive = !isCratePlacerActive
        }
    }
}

fun id(s: String) = Identifier.of("ion", s)