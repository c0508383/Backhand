package xonin.backhand;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

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
}
