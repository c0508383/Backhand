package xonin.backhand.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.core.BattlegearTranslator;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.packet.OffhandSwapPacket;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import xonin.backhand.Backhand;

import xonin.backhand.client.ClientEventHandler;

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

    @SubscribeEvent
    public void onClientTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null && !mc.isGamePaused() && event.phase == TickEvent.Phase.END) {
            ClientEventHandler.renderOffhandPlayer.itemRenderer.updateEquippedItem();
            ClientEventHandler.renderOffhandPlayer.updateFovModifierHand();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void clientHelper(TickEvent.PlayerTickEvent event) {
        ItemStack mainHandItem = event.player.getCurrentEquippedItem();
        ItemStack offhandItem = ((InventoryPlayerBattle) event.player.inventory).getOffhandItem();

        if (mainHandItem != null && (BattlegearUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
            return;
        }

        if (/*!MysteriumPatchesFixesO.shouldNotOverride && */event.player.worldObj.isRemote && Backhand.proxy.getLeftClickCounter() <= 0) {
            if (event.player.capabilities.allowEdit) {
                if (Backhand.proxy.isRightClickHeld()) { // if it's a block and we should try break it
                    MovingObjectPosition mop = BattlemodeHookContainerClass.getRaytraceBlock(event.player);
                    if (offhandItem != null && BattlemodeHookContainerClass.isItemBlock(offhandItem.getItem())) {
                        if (!BattlegearUtils.usagePriorAttack(offhandItem) && mop != null) {
                            BattlemodeHookContainerClass.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
                            Backhand.proxy.setLeftClickCounter(10);
                        } else {
                            Minecraft.getMinecraft().playerController.resetBlockRemoving();
                        }
                    } else {
                        if (mop != null && !BattlegearUtils.usagePriorAttack(offhandItem) && !canBlockBeInteractedWith(Minecraft.getMinecraft().theWorld, mop.blockX, mop.blockY, mop.blockZ)) {
                            BattlemodeHookContainerClass.tryBreakBlockOffhand(mop, offhandItem, mainHandItem, event);
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

    private static String[] activatedBlockMethodNames = {
            BattlegearTranslator.getMapedMethodName("Block", "func_149727_a", "onBlockActivated"),
            BattlegearTranslator.getMapedMethodName("Block", "func_149699_a", "onBlockClicked")};
    private static Class[][] activatedBlockMethodParams = {
            new Class[]{World.class, int.class, int.class, int.class, EntityPlayer.class, int.class, float.class, float.class, float.class},
            new Class[]{World.class, int.class, int.class, int.class, EntityPlayer.class}};
    @SuppressWarnings("unchecked")
    public static boolean canBlockBeInteractedWith(World worldObj, int x, int y, int z) {
        if (worldObj == null) return false;
        Block block = worldObj.getBlock(x, y, z);
        if (block == null) return false;
        if (block.getClass().equals(Block.class)) return false;
        try {
            Class c = block.getClass();
            while (!(c.equals(Block.class))) {
                try {
                    try {
                        c.getDeclaredMethod(activatedBlockMethodNames[0], activatedBlockMethodParams[0]);
                        return true;
                    } catch (NoSuchMethodException ignored) {
                    }

                    try {
                        c.getDeclaredMethod(activatedBlockMethodNames[1], activatedBlockMethodParams[1]);
                        return true;
                    } catch (NoSuchMethodException ignored) {
                    }
                } catch (NoClassDefFoundError ignored) {

                }

                c = c.getSuperclass();
            }

            return false;
        } catch (NullPointerException e) {
            return true;
        }
    }
}
