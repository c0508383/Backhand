package mods.battlegear2.api.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import xonin.backhand.Backhand;

public class ContainerPlayerBattle extends ContainerPlayer {

    public ContainerPlayerBattle(InventoryPlayer p_i1819_1_, boolean p_i1819_2_, EntityPlayer p_i1819_3_) {
        super(p_i1819_1_, p_i1819_2_, p_i1819_3_);
        if (Backhand.ExtraInventorySlot) {
            for (Object s : this.inventorySlots) {
                Slot slot = (Slot) s;
                if (slot instanceof SlotCrafting) {
                    slot.xDisplayPosition = 154;
                    slot.yDisplayPosition = 31;
                } else if (slot.inventory instanceof InventoryCrafting) {
                    slot.xDisplayPosition = 98 + (slot.slotNumber%2) * 18;
                    slot.yDisplayPosition = 21 + ((slot.slotNumber/2)%2) * 18;
                }
            }
            this.addSlotToContainer(new Slot(p_i1819_1_, InventoryPlayerBattle.OFFHAND_ITEM_INDEX, 80, 62));
        }
    }
}
