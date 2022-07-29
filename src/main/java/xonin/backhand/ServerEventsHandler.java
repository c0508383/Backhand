package xonin.backhand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import xonin.backhand.client.ClientEventHandler;
import xonin.backhand.client.ClientTickHandler;

public class ServerEventsHandler {

    @SubscribeEvent
    public void onItemUseStart(PlayerUseItemEvent.Start event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
        ItemStack mainhandItem = player.getCurrentEquippedItem();

        boolean offHandUse = BattlegearUtils.checkForRightClickFunction(offhandItem);
        boolean mainhandUse = BattlegearUtils.checkForRightClickFunction(mainhandItem);

        if (!offHandUse && !mainhandUse) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event)
    {
        if (ClientEventHandler.delay > 0) {
            ClientEventHandler.delay--;
        }

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
