package xonin.backhand;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ServerTickHandler {

    public ItemStack prevStackInSlot;
    public int blacklistDelay = -1;

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event)
    {
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(event.player);

        if (BattlegearUtils.hasOffhandInventory(event.player) && !Backhand.UseInventorySlot) {
            int slot = InventoryPlayerBattle.OFFHAND_ITEM_INDEX;
            if (offhandItem != event.player.inventory.getStackInSlot(slot)) {
                if (event.player.inventory.getStackInSlot(slot) == null || event.player.inventory.getStackInSlot(slot).stackSize == 0) {
                    BattlegearUtils.setPlayerOffhandItem(event.player, null);
                    event.player.inventory.setInventorySlotContents(slot, null);
                } else {
                    BattlegearUtils.setPlayerOffhandItem(event.player, BattlegearUtils.getOffhandItem(event.player));
                }
                event.player.inventory.markDirty();
            }
        } else if (event.phase == TickEvent.Phase.END) {
            if (blacklistDelay > 0) {
                blacklistDelay--;
            }
            if (Backhand.isOffhandBlacklisted(offhandItem)) {
                if (!ItemStack.areItemStacksEqual(offhandItem,prevStackInSlot)) {
                    blacklistDelay = 10;
                    event.player.inventoryContainer.detectAndSendChanges();
                }
                if (blacklistDelay == 0) {
                    BattlegearUtils.setPlayerOffhandItem(event.player,null);

                    boolean foundSlot = false;
                    for (int i = 0; i < event.player.inventory.getSizeInventory() - 4; i++) {
                        if (i == Backhand.AlternateOffhandSlot)
                            continue;
                        if (event.player.inventory.getStackInSlot(i) == null) {
                            event.player.inventory.setInventorySlotContents(i,offhandItem);
                            foundSlot = true;
                            break;
                        }
                    }
                    if (!foundSlot) {
                        event.player.entityDropItem(offhandItem,0);
                    }
                    event.player.inventoryContainer.detectAndSendChanges();
                }
            }
            prevStackInSlot = offhandItem;
        }

        if (ServerEventsHandler.arrowHotSwapped) {
            final ItemStack oldItem = event.player.getCurrentEquippedItem();
            if (offhandItem.getItem() != Items.arrow) {
                BattlegearUtils.setPlayerCurrentItem(event.player, offhandItem);
                BattlegearUtils.setPlayerOffhandItem(event.player, oldItem);
            }
            ServerEventsHandler.arrowHotSwapped = false;
        }
        if (ServerEventsHandler.totemHotSwapped) {
            final ItemStack oldItem = event.player.getCurrentEquippedItem();
            BattlegearUtils.setPlayerCurrentItem(event.player, offhandItem);
            BattlegearUtils.setPlayerOffhandItem(event.player, oldItem);
            ServerEventsHandler.totemHotSwapped = false;
        }
    }
}
