package xonin.backhand.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;

        if (ClientProxy.swapOffhand.getIsKeyPressed() && Keyboard.isKeyDown(Keyboard.getEventKey())) {
            ItemStack offhandItem = ((InventoryPlayerBattle) player.inventory).getOffhandItem();
            player.sendQueue.addToSendQueue(
                new OffhandSwapPacket(player.getCurrentEquippedItem(), offhandItem, player).generatePacket()
            );
            ((InventoryPlayerBattle) player.inventory).setOffhandItem(player.getCurrentEquippedItem());
            BattlegearUtils.setPlayerCurrentItem(player, offhandItem);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void clientHelper(TickEvent.PlayerTickEvent event) {
        if (!Backhand.proxy.isRightClickHeld()) {
            Backhand.proxy.setRightClickCounter(0);
        }

        ItemStack mainHandItem = event.player.getCurrentEquippedItem();
        ItemStack offhandItem = ((InventoryPlayerBattle) event.player.inventory).getOffhandItem();

        if (mainHandItem != null && (BattlegearUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
            return;
        }

        if (event.player.worldObj.isRemote && Backhand.proxy.getLeftClickCounter() <= 0) {
            if (event.player.capabilities.allowEdit) {
                if (Backhand.proxy.isRightClickHeld()) { // if it's a block and we should try break it
                    MovingObjectPosition mop = BattlemodeHookContainerClass.getRaytraceBlock(event.player);
                    if (offhandItem != null && BattlemodeHookContainerClass.isItemBlock(offhandItem.getItem())) {
                        if (!BattlegearUtils.usagePriorAttack(offhandItem) && mop != null) {
                            BattlegearClientTickHandler.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            Minecraft.getMinecraft().playerController.resetBlockRemoving();
                        }
                    } else {
                        if (mop != null && !BattlegearUtils.usagePriorAttack(offhandItem) && !BattlemodeHookContainerClass.canBlockBeInteractedWith(Minecraft.getMinecraft().theWorld, mop.blockX, mop.blockY, mop.blockZ)) {
                            BattlegearClientTickHandler.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            Minecraft.getMinecraft().playerController.resetBlockRemoving();
                        }
                    }
                } else if (!Backhand.proxy.isLeftClickHeld()) {
                    Minecraft.getMinecraft().playerController.resetBlockRemoving();
                }
            }
        }
    }
}
