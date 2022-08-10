package xonin.backhand.client;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.InvTweaks;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.client.BattlegearClientTickHandler;
import mods.battlegear2.packet.OffhandSwapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import xonin.backhand.Backhand;

public class ClientTickHandler {
    public static int delay;
    public static int swapDelay;
    public static boolean prevInvTweaksAutoRefill;
    public static boolean prevInvTweaksBreakRefill;

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;

        if (ClientProxy.swapOffhand.getIsKeyPressed() && Keyboard.isKeyDown(Keyboard.getEventKey()) && swapDelay <= 0) {
            ItemStack offhandItem = ((InventoryPlayerBattle) player.inventory).getOffhandItem();
            if (Backhand.isOffhandBlacklisted(player.getCurrentEquippedItem()) || Backhand.isOffhandBlacklisted(offhandItem)) {
                return;
            }
            swapDelay = 5;
            try {
                this.getClass().getMethod("invTweaksSwapPatch");
                invTweaksSwapPatch();
            } catch (Exception ignored) {}

            player.sendQueue.addToSendQueue(
                new OffhandSwapPacket(player.getCurrentEquippedItem(), offhandItem, player).generatePacket()
            );
            ((InventoryPlayerBattle) player.inventory).setOffhandItem(player.getCurrentEquippedItem());
            BattlegearUtils.setPlayerCurrentItem(player, offhandItem);
        }
    }

    @Optional.Method(modid="inventorytweaks")
    public void invTweaksSwapPatch() {
        prevInvTweaksAutoRefill = Boolean.parseBoolean(InvTweaks.getConfigManager().getConfig().getProperty("enableAutoRefill"));
        prevInvTweaksBreakRefill = Boolean.parseBoolean(InvTweaks.getConfigManager().getConfig().getProperty("autoRefillBeforeBreak"));
        InvTweaks.getConfigManager().getConfig().setProperty("enableAutoRefill", "false");
        InvTweaks.getConfigManager().getConfig().setProperty("autoRefillBeforeBreak","false");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (swapDelay > 0) {
            swapDelay--;
            if (swapDelay == 0) {
                try {
                    this.getClass().getMethod("restoreInvTweaksConfigs");
                    restoreInvTweaksConfigs();
                } catch (Exception ignored) {}
            }
        }
    }

    @Optional.Method(modid="inventorytweaks")
    public void restoreInvTweaksConfigs() {
        InvTweaks.getConfigManager().getConfig().setProperty("enableAutoRefill",String.valueOf(prevInvTweaksAutoRefill));
        InvTweaks.getConfigManager().getConfig().setProperty("autoRefillBeforeBreak",String.valueOf(prevInvTweaksBreakRefill));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void clientHelper(TickEvent.PlayerTickEvent event) {
        if (ClientTickHandler.delay > 0) {
            ClientTickHandler.delay--;
        }

        if (!Backhand.OffhandBreakBlocks) {
            return;
        }

        if (!Backhand.EmptyOffhand && BattlegearUtils.getOffhandItem(event.player) == null) {
            return;
        }

        if (!Backhand.proxy.isRightClickHeld()) {
            Backhand.proxy.setRightClickCounter(0);
        }

        ItemStack mainHandItem = event.player.getCurrentEquippedItem();
        ItemStack offhandItem = ((InventoryPlayerBattle) event.player.inventory).getOffhandItem();

        if (mainHandItem != null && (BattlegearUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (event.player.worldObj.isRemote && Backhand.proxy.getLeftClickCounter() <= 0 && mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            if (event.player.capabilities.allowEdit) {
                if (Backhand.proxy.isRightClickHeld()) { // if it's a block and we should try break it
                    MovingObjectPosition mop = BattlemodeHookContainerClass.getRaytraceBlock(event.player);
                    if (offhandItem != null && BattlemodeHookContainerClass.isItemBlock(offhandItem.getItem())) {
                        if (!BattlegearUtils.usagePriorAttack(offhandItem) && mop != null) {
                            BattlegearClientTickHandler.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            mc.playerController.resetBlockRemoving();
                        }
                    } else {
                        if (mop != null && !BattlegearUtils.usagePriorAttack(offhandItem) && !BattlemodeHookContainerClass.canBlockBeInteractedWith(mc.theWorld, mop.blockX, mop.blockY, mop.blockZ)) {
                            BattlegearClientTickHandler.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            mc.playerController.resetBlockRemoving();
                        }
                    }
                } else if (!Backhand.proxy.isLeftClickHeld()) {
                    mc.playerController.resetBlockRemoving();
                }
            }
        }
    }
}
