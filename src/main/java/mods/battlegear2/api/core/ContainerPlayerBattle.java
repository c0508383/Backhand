package mods.battlegear2.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import xonin.backhand.Backhand;

public class ContainerPlayerBattle extends ContainerPlayer {

    public ContainerPlayerBattle(InventoryPlayer p_i1819_1_, boolean p_i1819_2_, EntityPlayer p_i1819_3_) {
        super(p_i1819_1_, p_i1819_2_, p_i1819_3_);
        if (Backhand.ExtraInventorySlot && !Backhand.UseInventorySlot && BattlegearUtils.hasOffhandInventory(p_i1819_3_)) {
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
            this.addSlotToContainer(new OffhandSlot(p_i1819_1_, -1, 80, 62));
        }
    }

    public ItemStack slotClick(int slot, int p_75144_2_, int p_75144_3_, EntityPlayer player)
    {
        if (!(player.inventoryContainer instanceof ContainerPlayerBattle) && slot == 45) {
            return null;
        }
        return super.slotClick(slot, p_75144_2_, p_75144_3_, player);
    }

    public static class OffhandSlot extends Slot
    {
        public OffhandSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }

        public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_)
        {
            if (p_75220_1_ != null && p_75220_2_ != null)
            {
                if (p_75220_1_.getItem() == p_75220_2_.getItem())
                {
                    int i = p_75220_2_.stackSize - p_75220_1_.stackSize;

                    if (i > 0)
                    {
                        this.onCrafting(p_75220_1_, i);
                    }
                }
            }
        }

        public ItemStack getStack()
        {
            return BattlegearUtils.getOffhandItem(((InventoryPlayer)this.inventory).player);
        }

        public void putStack(ItemStack p_75215_1_)
        {
            BattlegearUtils.setPlayerOffhandItem(((InventoryPlayer)this.inventory).player,p_75215_1_);
            this.onSlotChanged();
        }

        public ItemStack decrStackSize(int p_75209_1_)
        {
            ItemStack offhandItem = BattlegearUtils.getOffhandItem(((InventoryPlayer)this.inventory).player);
            offhandItem = offhandItem.splitStack(p_75209_1_);
            if (offhandItem.stackSize == 0) {
                return null;
            }
            return offhandItem;
        }

        public boolean isItemValid(ItemStack p_75214_1_)
        {
            return !Backhand.isOffhandBlacklisted(p_75214_1_);
        }
    }
}
