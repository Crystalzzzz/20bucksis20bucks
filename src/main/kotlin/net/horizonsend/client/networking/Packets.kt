package net.horizonsend.client.networking

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.horizonsend.client.id
import net.horizonsend.client.networking.packets.GetCurrentWorld
import net.horizonsend.client.networking.packets.HandshakePacket
import net.horizonsend.client.networking.packets.PlayerAdd
import net.horizonsend.client.networking.packets.PlayerRemove
import net.horizonsend.client.networking.packets.ShipData
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

abstract class IonPacketHandler {
    abstract val name: String
    val id by lazy { id(name.lowercase()) }
    val payloadId by lazy { CustomPayload.Id<IonPayload>(id) }

    open fun s2c(
        client: MinecraftClient,
        handler: ClientPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {}

    open fun c2s(buf: PacketByteBuf) {}
}

class IonPayload(val handler: IonPacketHandler, val buf: PacketByteBuf) : CustomPayload {
    override fun getId(): CustomPayload.Id<IonPayload> = handler.payloadId

    companion object {
        fun codec(handler: IonPacketHandler): PacketCodec<RegistryByteBuf, IonPayload> =
            PacketCodec.of(
                { payload, buf -> payload.handler.c2s(buf) },
                { buf ->
                    val copy = PacketByteBufs.create()
                    copy.writeBytes(buf.readBytes(buf.readableBytes()))
                    IonPayload(handler, copy)
                }
            )
    }
}

enum class Packets(val handler: IonPacketHandler) {
    HANDSHAKE(HandshakePacket),
    PLAYER_ADD(PlayerAdd),
    SHIP_DATA(ShipData.ShipDataPacket),
    PLAYER_REMOVE(PlayerRemove),
    GET_CURRENT_WORLD(GetCurrentWorld.GetCurrentWorld);

    fun send() {
        val buf = PacketByteBufs.create()
        handler.c2s(buf)
        ClientPlayNetworking.send(IonPayload(handler, buf))
    }
}