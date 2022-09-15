package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "AntiVoid",description = "Anti you fall into the void or fall too long",category = ModuleCategory.PLAYER,keyBind = Keyboard.KEY_Y)
public class AntiVoid extends Module {
    private final ListValue modeValue = new ListValue("Mode",new String[]{"MotionFlag"},"MotionFlag");
    private final FloatValue maxFallDistanceValue = new FloatValue("MaxFallDistance",10,5,20);
    private final IntegerValue coolDownTicksValue = new IntegerValue("CoolDownTicks",20,5,60);
    private final BoolValue autoScaffoldValue = new BoolValue("AutoScaffold",true);
    private final FloatValue motionFlag_MotionYValue = new FloatValue("MotionFlag-MotionY",2,1,5);

    private int ticks;
    private boolean coolDown;

    @EventTarget
    public void onUpdate(UpdateEvent updateEvent) {
        if (ticks > coolDownTicksValue.get()) {
            coolDown = false;
            ticks = 0;
        }
        if (modeValue.get().equalsIgnoreCase("motionFlag")) {
            if (mc.thePlayer.fallDistance > maxFallDistanceValue.get() && mc.theWorld
                    .getBlockState(new BlockPos(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 1,
                            mc.thePlayer.posZ
                    )).getBlock() == Blocks.air && !coolDown) {
                mc.thePlayer.motionY = motionFlag_MotionYValue.get();
                if (autoScaffoldValue.get()) {
                    LiquidBounce.moduleManager.getModule(Scaffold.class).setState(true);
                }
                coolDown = true;
            }
        }

        ticks++;
    }


}
