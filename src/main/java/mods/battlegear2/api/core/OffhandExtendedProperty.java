package mods.battlegear2.api.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class OffhandExtendedProperty implements IExtendedEntityProperties {
    public EntityPlayer player;
    public boolean offhandItemChanged = true;
    private ItemStack offhandItem;

    public OffhandExtendedProperty(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setTag("OffhandItemStack", offhandItem.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        this.setOffhandItem(ItemStack.loadItemStackFromNBT(compound.getCompoundTag("OffhandItemStack")));
    }

    @Override
    public void init(Entity entity, World world) {
    }

    public ItemStack getOffhandItem(){
        return offhandItem;
    }

    public void setOffhandItem(ItemStack stack) {
        if (!ItemStack.areItemStacksEqual(stack,this.offhandItem)) {
            this.offhandItemChanged = true;
        }
        this.offhandItem = stack;
    }
}
