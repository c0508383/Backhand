package xonin.backhand;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ServerTickHandler {

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event)
    {
        ItemStack itemstack = BattlegearUtils.getOffhandItem(event.player);
        if (BattlegearUtils.hasOffhandInventory(event.player)) {
            if (itemstack != event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX)) {
                if (event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX) == null || event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX).stackSize == 0) {
                    BattlegearUtils.setPlayerOffhandItem(event.player, null);
                    event.player.inventory.setInventorySlotContents(InventoryPlayerBattle.OFFHAND_ITEM_INDEX, null);
                } else {
                    BattlegearUtils.setPlayerOffhandItem(event.player, BattlegearUtils.getOffhandItem(event.player));
                }
                event.player.inventory.markDirty();
            }
        }

        if (ServerEventsHandler.arrowHotSwapped) {
            final ItemStack oldItem = event.player.getCurrentEquippedItem();
            final ItemStack offhandItem = BattlegearUtils.getOffhandItem(event.player);
            if (offhandItem.getItem() != Items.arrow) {
                BattlegearUtils.setPlayerCurrentItem(event.player, offhandItem);
                BattlegearUtils.setPlayerOffhandItem(event.player, oldItem);
            }
            ServerEventsHandler.arrowHotSwapped = false;
        }
        if (ServerEventsHandler.totemHotSwapped) {
            final ItemStack oldItem = event.player.getCurrentEquippedItem();
            final ItemStack offhandItem = BattlegearUtils.getOffhandItem(event.player);
            BattlegearUtils.setPlayerCurrentItem(event.player, offhandItem);
            BattlegearUtils.setPlayerOffhandItem(event.player, oldItem);
            ServerEventsHandler.totemHotSwapped = false;
        }
    }
}
