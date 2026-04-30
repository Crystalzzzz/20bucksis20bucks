package net.horizonsend.client.features

import net.horizonsend.client.Void.isCratePlacerActive
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object CratePlacer {
    fun handleCratePlacer() {
        if (!isCratePlacerActive) return

        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return
        val world = mc.world ?: return
        val interactionManager = mc.interactionManager ?: return

        val px = player.x.toInt()
        val py = player.y.toInt()
        val pz = player.z.toInt()

        for (x in (px - 5)..(px + 5)) {
            for (y in (py - 5)..(py + 5)) {
                for (z in (pz - 5)..(pz + 5)) {
                    val pistonPos = BlockPos(x, y, z)
                    val pistonState = world.getBlockState(pistonPos)

                    if (pistonState.block != Blocks.STICKY_PISTON) continue

                    val facing = pistonState.get(Properties.FACING)
                    val addPos = facing.vector
                    val cratePos = Vec3d(
                        (x + addPos.x).toDouble(),
                        (y + addPos.y).toDouble(),
                        (z + addPos.z).toDouble()
                    )

                    if (!cratePos.isInRange(player.pos, 5.0)) continue

                    val crateBlockPos = BlockPos(
                        cratePos.x.toInt(),
                        cratePos.y.toInt(),
                        cratePos.z.toInt()
                    )

                    // Skip if shulker already there
                    if (world.getBlockState(crateBlockPos).isIn(BlockTags.SHULKER_BOXES)) continue

                    // If not holding a shulker box, find one in inventory
                    if (!player.getStackInHand(Hand.MAIN_HAND).item.translationKey.endsWith("shulker_box")) {
                        var found = false
                        for (invSlot in 40 downTo 1) {
                            if (player.inventory.getStack(invSlot).item.translationKey.endsWith("shulker_box")) {
                                interactionManager.clickSlot(
                                    player.playerScreenHandler.syncId,
                                    invSlot,
                                    0,
                                    SlotActionType.PICKUP,
                                    player
                                )
                                found = true
                                break
                            }
                        }
                        if (!found) continue
                    }

                    // Place the shulker box
                    player.inventory.selectedSlot = 8
                    interactionManager.interactBlock(
                        player,
                        Hand.MAIN_HAND,
                        BlockHitResult(
                            player.pos,
                            facing,
                            pistonPos,
                            false
                        )
                    )
                    return // Only place one per tick
                }
            }
        }
    }
}