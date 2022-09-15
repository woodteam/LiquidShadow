/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.SlowDownEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoSlow", description = "Cancels slowness effects caused by soulsand and using items.",
        category = ModuleCategory.MOVEMENT)
class NoSlow : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla","NCP","AAC5-NotBeenTested"),"NCP")

    // Highly customizable values

    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)

    // Blocks
    val soulsandValue = BoolValue("Soulsand", true)
    val liquidPushValue = BoolValue("LiquidPush", true)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return
        val heldItem = thePlayer.heldItem ?: return

        if (heldItem.item !is ItemSword || !MovementUtils.isMoving)
            return

        val aura = LiquidBounce.moduleManager[KillAura::class.java] as KillAura
        if (!thePlayer.isBlocking && !aura.blockingStatus)
            return

        if (modeValue.get().equals("NCP",true)) {
            when (event.eventState) {
                EventState.PRE -> {
                    val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(0, 0, 0), EnumFacing.DOWN)
                    mc.netHandler.addToSendQueue(digging)
                }
                EventState.POST -> {
                    val blockPlace = C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer!!.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F)
                    mc.netHandler.addToSendQueue(blockPlace)
                }
            }
        }
        if (modeValue.get().equals("AAC5-NotBeenTested",true)) {
            val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)
            if (killAura is KillAura) {
                if (event.eventState == EventState.POST && (mc.thePlayer.isUsingItem || mc.thePlayer.isBlocking || killAura.blockingStatus)) {
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
                }
            }
            return
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer!!.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        return when {
            item is ItemFood || item is ItemPotion || item is ItemBucketMilk -> {
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
            }
            item is ItemSword -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }
            item is ItemBow -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }
            else -> 0.2F
        }
    }

}
