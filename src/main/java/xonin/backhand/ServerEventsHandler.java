package xonin.backhand;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

public class ServerEventsHandler {

    public static boolean arrowHotSwapped = false;
    public static boolean totemHotSwapped = false;
    public static int fireworkHotSwapped = -1;

    @SubscribeEvent
    public void onPlayerInteractNonVanilla(PlayerInteractEvent event) {
        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            EntityPlayer player = event.entityPlayer;
            ItemStack mainhandItem = player.getHeldItem();
            ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
            if((mainhandItem == null || mainhandItem.getItem() != Items.fireworks) && offhandItem != null && offhandItem.getItem() == Items.fireworks) {
                BattlegearUtils.swapOffhandItem(player);
                fireworkHotSwapped = 1;
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (!BattlegearUtils.hasOffhandInventory(player)) {
            ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
            player.func_146097_a(offhandItem, true, false);
            BattlegearUtils.setPlayerOffhandItem(player,null);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer) || event.entityLiving.getHealth() - event.ammount > 0)
            return;
        try {
            Class<?> totemItem = Class.forName("ganymedes01.etfuturum.items.ItemTotemUndying");

            EntityPlayer player = (EntityPlayer) event.entityLiving;
            ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
            ItemStack mainhandItem = player.getCurrentEquippedItem();
            if (offhandItem == null) {
                return;
            }

            if (totemItem.isInstance(offhandItem.getItem()) && (mainhandItem == null || !totemItem.isInstance(mainhandItem.getItem()))) {
                BattlegearUtils.swapOffhandItem(player);
                totemHotSwapped = true;
                MinecraftForge.EVENT_BUS.post(event);
            }
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public void onItemUseStart(PlayerUseItemEvent.Start event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
        ItemStack mainhandItem = player.getCurrentEquippedItem();

        //boolean offHandUse = BattlegearUtils.checkForRightClickFunction(offhandItem);
        boolean mainhandUse = BattlegearUtils.checkForRightClickFunction(mainhandItem);

        if (offhandItem != null && !mainhandUse) {
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
                if (offhandItem.getItem() != Items.arrow) {
                    BattlegearUtils.swapOffhandItem(event.entityPlayer);
                }
            }
        }
    }
}
