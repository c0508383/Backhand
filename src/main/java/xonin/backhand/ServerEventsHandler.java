package xonin.backhand;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

public class ServerEventsHandler {

    public static boolean arrowHotSwapped = false;

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

    @SubscribeEvent
    public void onArrowNock(ArrowNockEvent event) {
        if (!Backhand.UseOffhandArrows) {
            return;
        }

        boolean overrideWithOffhand = false;
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(event.entityPlayer);
        if (offhandItem != null) {
            try {
                Class<?> etFuturumArrow = Class.forName("ganymedes01.etfuturum.items.ItemArrowTipped");
                if (etFuturumArrow.isInstance(offhandItem.getItem())) {
                    overrideWithOffhand = true;
                }
            } catch (Exception ignored) {}

            if (Items.arrow == offhandItem.getItem()) {
                overrideWithOffhand = true;
            }

            if (overrideWithOffhand) {
                event.setCanceled(true);
                event.entityPlayer.setItemInUse(event.result, event.result.getItem().getMaxItemUseDuration(event.result));
            }
        }
    }

    @SubscribeEvent
    public void onPreArrowLoose(PlayerUseItemEvent.Stop event) {
        if (!Backhand.UseOffhandArrows || !(event.item.getItem() instanceof ItemBow)) {
            return;
        }

        boolean overrideWithOffhand = false;
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(event.entityPlayer);
        if (offhandItem != null) {
            try {
                Class<?> etFuturumArrow = Class.forName("ganymedes01.etfuturum.items.ItemArrowTipped");
                if (etFuturumArrow.isInstance(offhandItem.getItem())) {
                    overrideWithOffhand = true;
                }
            } catch (Exception ignored) {}

            if (Items.arrow == offhandItem.getItem()) {
                overrideWithOffhand = true;
            }

            if (overrideWithOffhand) {
                arrowHotSwapped = true;
                final ItemStack oldItem = event.entityPlayer.getCurrentEquippedItem();
                BattlegearUtils.setPlayerCurrentItem(event.entityPlayer,offhandItem);
                BattlegearUtils.setPlayerOffhandItem(event.entityPlayer,oldItem);
            }
        }
    }
}
