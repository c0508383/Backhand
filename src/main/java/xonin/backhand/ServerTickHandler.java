package xonin.backhand;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.packet.BattlegearSyncItemPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ServerTickHandler {

    public ItemStack prevStackInSlot;
    public int blacklistDelay = -1;

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer player = event.player;
        ItemStack mainHandItem = player.getCurrentEquippedItem();
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);

        if (mainHandItem != null && player.getItemInUse() == offhandItem && (BattlegearUtils.checkForRightClickFunction(mainHandItem)
                || BattlemodeHookContainerClass.isItemBlock(mainHandItem.getItem()))) {
            player.clearItemInUse();
        }

        if (BattlegearUtils.hasOffhandInventory(player) && !Backhand.UseInventorySlot) {
            int slot = InventoryPlayerBattle.OFFHAND_ITEM_INDEX;
            if (offhandItem != player.inventory.getStackInSlot(slot)) {
                if (player.inventory.getStackInSlot(slot) == null || player.inventory.getStackInSlot(slot).stackSize == 0) {
                    BattlegearUtils.setPlayerOffhandItem(player, null);
                    player.inventory.setInventorySlotContents(slot, null);
                } else {
                    BattlegearUtils.setPlayerOffhandItem(player, BattlegearUtils.getOffhandItem(player));
                }
                player.inventory.markDirty();
            }
        } else if (event.phase == TickEvent.Phase.END) {
            if (blacklistDelay > 0) {
                blacklistDelay--;
            }
            if (Backhand.isOffhandBlacklisted(offhandItem)) {
                if (!ItemStack.areItemStacksEqual(offhandItem,prevStackInSlot)) {
                    blacklistDelay = 10;
                    player.inventoryContainer.detectAndSendChanges();
                }
                if (blacklistDelay == 0) {
                    BattlegearUtils.setPlayerOffhandItem(player,null);

                    boolean foundSlot = false;
                    for (int i = 0; i < player.inventory.getSizeInventory() - 4; i++) {
                        if (i == Backhand.AlternateOffhandSlot)
                            continue;
                        if (player.inventory.getStackInSlot(i) == null) {
                            player.inventory.setInventorySlotContents(i,offhandItem);
                            foundSlot = true;
                            break;
                        }
                    }
                    if (!foundSlot) {
                        player.entityDropItem(offhandItem,0);
                    }
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
            prevStackInSlot = offhandItem;
        }

        if (player.inventory instanceof InventoryPlayerBattle && ((InventoryPlayerBattle)player.inventory).offhandItemChanged) {
            Backhand.packetHandler.sendPacketToAll(new BattlegearSyncItemPacket(player).generatePacket());
            ((InventoryPlayerBattle)player.inventory).offhandItemChanged = false;
        }

        if (ServerEventsHandler.arrowHotSwapped) {
            if (offhandItem.getItem() != Items.arrow) {
                BattlegearUtils.swapOffhandItem(player);
            }
            ServerEventsHandler.arrowHotSwapped = false;
        }
        if (ServerEventsHandler.totemHotSwapped) {
            ServerEventsHandler.totemHotSwapped = false;
        }

        if (ServerEventsHandler.fireworkHotSwapped > 0) {
            ServerEventsHandler.fireworkHotSwapped--;
        } else if (ServerEventsHandler.fireworkHotSwapped == 0) {
            BattlegearUtils.swapOffhandItem(player);
            ServerEventsHandler.fireworkHotSwapped--;
            MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                    (int)player.posX, (int)player.posY, (int)player.posZ, -1, player.worldObj));
            BattlegearUtils.swapOffhandItem(player);
        }
    }
}
