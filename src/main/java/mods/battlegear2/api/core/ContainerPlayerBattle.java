package mods.battlegear2.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;

public class ContainerPlayerBattle extends ContainerPlayer {

    public ContainerPlayerBattle(InventoryPlayer p_i1819_1_, boolean p_i1819_2_, EntityPlayer p_i1819_3_) {
        super(p_i1819_1_, p_i1819_2_, p_i1819_3_);
        this.addSlotToContainer(new Slot(p_i1819_1_, InventoryPlayerBattle.OFFHAND_ITEM_INDEX, 88,64));
    }
}
