package mods.battlegear2.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import xonin.backhand.Backhand;

public class ContainerPlayerBattle extends ContainerPlayer {

    public ContainerPlayerBattle(InventoryPlayer p_i1819_1_, boolean p_i1819_2_, EntityPlayer p_i1819_3_) {
        super(p_i1819_1_, p_i1819_2_, p_i1819_3_);
        if (Backhand.ExtraInventorySlot && BattlegearUtils.hasOffhandInventory(p_i1819_3_)) {
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
            this.addSlotToContainer(new OffhandSlot(p_i1819_1_, InventoryPlayerBattle.OFFHAND_ITEM_INDEX, 80, 62));
        }
    }

    public static class OffhandSlot extends Slot
    {
        public OffhandSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }

        public boolean isItemValid(ItemStack p_75214_1_)
        {
            return !Backhand.isOffhandBlacklisted(p_75214_1_);
        }
    }
}
