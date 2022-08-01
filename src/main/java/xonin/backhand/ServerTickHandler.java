package xonin.backhand;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.item.ItemStack;

public class ServerTickHandler {

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event)
    {
        ItemStack itemstack = ((InventoryPlayerBattle)event.player.inventory).getOffhandItem();
        if (itemstack != event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX)) {
            if (event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX) == null || event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX).stackSize == 0) {
                ((InventoryPlayerBattle)event.player.inventory).setInventorySlotContents(InventoryPlayerBattle.OFFHAND_ITEM_INDEX, null);
                event.player.inventory.setInventorySlotContents(InventoryPlayerBattle.OFFHAND_ITEM_INDEX, null);
            }
            else ((InventoryPlayerBattle)event.player.inventory).setInventorySlotContents(InventoryPlayerBattle.OFFHAND_ITEM_INDEX, event.player.inventory.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX));
            event.player.inventory.markDirty();
        }
    }
}
