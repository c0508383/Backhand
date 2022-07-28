package xonin.backhand;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BackhandUtilPlayer {
    public static ItemStack getInventoryOffhandItem(EntityPlayer player) {
        return player.inventory.getStackInSlot(Backhand.OffhandInventorySlot);
    }
}
