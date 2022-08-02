package mods.battlegear2.api.core;

import mods.battlegear2.packet.BattlegearSyncItemPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import xonin.backhand.Backhand;

/**
 * User: nerd-boy
 * Date: 15/07/13
 * Time: 3:08 PM
 * Replacement for the player inventory
 */
public class InventoryPlayerBattle extends InventoryPlayer {

    public boolean hasChanged = true;
    public static int ARMOR_OFFSET = 100;
    public static int OFFSET = 150;

    public static final int OFFHAND_ITEM_INDEX = 40;
    public static final int OFFHAND_HOTBAR_SLOT = 9;
    public ItemStack offhandItem;

    public InventoryPlayerBattle(EntityPlayer entityPlayer) {
        super(entityPlayer);
    }

    public int clearInventory(Item item, int metadata) {
        int amount = 0;
        ItemStack itemstack = offhandItem;
        if (itemstack != null && (item == null || itemstack.getItem() == item) && (metadata <= -1 || itemstack.getItemDamage() == metadata))
        {
            amount += itemstack.stackSize;
            offhandItem = null;
        }

        return amount + super.clearInventory(item,metadata);
    }

    public boolean addItemStackToInventory(final ItemStack itemStack)
    {
        if (!Backhand.isOffhandBlacklisted(itemStack)) {
            if (offhandItem == null && getFirstEmptyStack() == -1) {
                offhandItem = itemStack;
                itemStack.stackSize = 0;
                Backhand.packetHandler.sendPacketToPlayer(new BattlegearSyncItemPacket(player).generatePacket(), (EntityPlayerMP) player);
                return true;
            }

            if (offhandItem != null && offhandItem.getItem() == itemStack.getItem() && offhandItem.isStackable() && offhandItem.stackSize < offhandItem.getMaxStackSize() && offhandItem.stackSize < this.getInventoryStackLimit() && (!offhandItem.getHasSubtypes() || offhandItem.getItemDamage() == itemStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(offhandItem, itemStack)) {
                if (offhandItem.stackSize + itemStack.stackSize > offhandItem.getMaxStackSize()) {
                    itemStack.stackSize -= offhandItem.stackSize;
                    offhandItem.stackSize = offhandItem.getMaxStackSize();
                    Backhand.packetHandler.sendPacketToPlayer(new BattlegearSyncItemPacket(player).generatePacket(), (EntityPlayerMP) player);
                    return super.addItemStackToInventory(itemStack);
                } else {
                    offhandItem.stackSize += itemStack.stackSize;
                    itemStack.stackSize = 0;
                    Backhand.packetHandler.sendPacketToPlayer(new BattlegearSyncItemPacket(player).generatePacket(), (EntityPlayerMP) player);
                    return true;
                }
            }
        }
        return super.addItemStackToInventory(itemStack);
    }

    public boolean consumeInventoryItem(Item item)
    {
        if (this.offhandItem != null && this.offhandItem.getItem() == item)
        {
            if (--this.offhandItem.stackSize <= 0)
            {
                this.offhandItem = null;
            }
            return true;
        } else {
            return super.consumeInventoryItem(item);
        }
    }

    public boolean hasItem(Item item)
    {
        if (this.offhandItem != null && this.offhandItem.getItem() == item)
        {
            return true;
        }

        return super.hasItem(item);
    }

    public ItemStack getCurrentItem()
    {
        return this.currentItem < 9 && this.currentItem >= 0 ? this.mainInventory[this.currentItem] : this.currentItem == InventoryPlayerBattle.OFFHAND_HOTBAR_SLOT ? getOffhandItem() : null;
    }

    /**
     * Patch used for "set current slot" vanilla packets
     * @param id the value to test for currentItem setting
     * @return true if it is possible for currentItem to be set with this value
     */
    public static boolean isValidSwitch(int id) {
        return (id >= 0 && id < getHotbarSize()) || id == OFFHAND_HOTBAR_SLOT;
    }

    public ItemStack decrStackSize(int slot, int amount)
    {
        if (slot < InventoryPlayerBattle.OFFHAND_ITEM_INDEX) {
            return super.decrStackSize(slot,amount);
        } else {
            if (offhandItem != null) {
                ItemStack itemstack;

                if (offhandItem.stackSize <= amount) {
                    itemstack = offhandItem;
                    offhandItem = null;
                    return itemstack;
                } else {
                    itemstack = offhandItem.splitStack(amount);

                    if (offhandItem.stackSize == 0) {
                        offhandItem = null;
                    }

                    return itemstack;
                }
            } else {
                return null;
            }
        }
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot < InventoryPlayerBattle.OFFHAND_ITEM_INDEX) {
            return super.getStackInSlot(slot);
        } else if (slot == InventoryPlayerBattle.OFFHAND_ITEM_INDEX) {
            return this.getOffhandItem();
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        if (slot == InventoryPlayerBattle.OFFHAND_ITEM_INDEX) {
            this.setOffhandItem(itemStack);
        } else {
            super.setInventorySlotContents(slot,itemStack);
        }
    }

    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot == InventoryPlayerBattle.OFFHAND_ITEM_INDEX) {
            return this.getOffhandItem();
        } else {
            return super.getStackInSlotOnClosing(slot);
        }
    }

    /**
     * Writes the inventory out as a list of compound tags. This is where the slot indices are used (+100 for armor, +150
     * for battle slots).
     */
    @Override
    public NBTTagList writeToNBT(NBTTagList par1nbtTagList) {
        NBTTagList nbtList = super.writeToNBT(par1nbtTagList);
        NBTTagCompound nbttagcompound;

        if (offhandItem != null) {
            nbttagcompound = new NBTTagCompound();
            //This will be -ve, but meh still works
            nbttagcompound.setByte("Slot", (byte) (OFFSET));
            this.offhandItem.writeToNBT(nbttagcompound);
            nbtList.appendTag(nbttagcompound);
        }
        return nbtList;
    }

    /**
     * Reads from the given tag list, resize each arrays to maximum required and fills the slots in the inventory with the correct items.
     */
    @Override
    public void readFromNBT(NBTTagList nbtTagList) {
        int highestMain = mainInventory.length, highestArmor = armorInventory.length;
        for (int i = 0; i < nbtTagList.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            if (j >= 0 && j < ARMOR_OFFSET) {
                if(j >= highestMain)
                    highestMain = j + 1;
            }
            else if (j >= ARMOR_OFFSET && j < OFFSET) {
                if(j - ARMOR_OFFSET >= highestArmor)
                    highestArmor = j + 1 - ARMOR_OFFSET;
            }
        }
        this.mainInventory = new ItemStack[highestMain];
        this.armorInventory = new ItemStack[highestArmor];
        for (int i = 0; i < nbtTagList.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

            if (itemstack != null) {
                if (j < this.mainInventory.length) {
                    this.mainInventory[j] = itemstack;
                }
                else if (j >= ARMOR_OFFSET && j - ARMOR_OFFSET < this.armorInventory.length) {
                    this.armorInventory[j - ARMOR_OFFSET] = itemstack;
                }
                else if (j >= OFFSET) {
                    this.offhandItem = itemstack;
                }
                /*else{
                    MinecraftForge.EVENT_BUS.post(new UnhandledInventoryItemEvent(player, j, itemstack));
                }*/
            }
        }
    }

    /**
     * Copy the slots content from another instance, usually for changing dimensions
     * @param par1InventoryPlayer the instance to copy from
     */
    @Override
    public void copyInventory(InventoryPlayer par1InventoryPlayer) {
        this.mainInventory = new ItemStack[par1InventoryPlayer.mainInventory.length];
        this.armorInventory = new ItemStack[par1InventoryPlayer.armorInventory.length];
        super.copyInventory(par1InventoryPlayer);
        if (par1InventoryPlayer instanceof InventoryPlayerBattle) {
            hasChanged = true;
            this.offhandItem = ItemStack.copyItemStack(par1InventoryPlayer.getStackInSlot(InventoryPlayerBattle.OFFHAND_ITEM_INDEX));
        }
    }

    public void dropAllItems()
    {
        super.dropAllItems();
        this.player.func_146097_a(offhandItem, true, false);
        this.offhandItem = null;
    }

    /**
     * Get the offset item (for the left hand)
     * @return the item held in left hand, if any
     */
    public ItemStack getOffhandItem(){
        return offhandItem;
    }

    public void setOffhandItem(ItemStack stack) {
        this.offhandItem = stack;
    }
}
