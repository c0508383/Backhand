package louisxiv.backhand;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BackhandUtilPlayer {

    public static ItemStack getOffhandItem(EntityPlayer player) {
        return player.inventory.getStackInSlot(Backhand.OffhandInventorySlot);
    }

    public static void setPlayerCurrentItem(EntityPlayer player, ItemStack stack) {
        (player.inventory).setInventorySlotContents(player.inventory.currentItem, stack);
    }

    public static void setOffhandItem(EntityPlayer player, ItemStack stack){
        player.inventory.setInventorySlotContents(Backhand.OffhandInventorySlot, stack);
    }
}
