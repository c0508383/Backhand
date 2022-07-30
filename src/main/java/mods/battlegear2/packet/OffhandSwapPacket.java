package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import xonin.backhand.Backhand;

public class OffhandSwapPacket extends AbstractMBPacket {
    public static final String packetName = "MB2|Swap";

    private ItemStack offhandItem;
    private ItemStack mainItem;
    private String user;
    EntityPlayer player;

    public OffhandSwapPacket(ItemStack offhandItem, ItemStack mainItem, EntityPlayer player) {
        this.offhandItem = offhandItem;
        this.mainItem = mainItem;
        this.player = player;
        this.user = player.getCommandSenderName();
    }

    public OffhandSwapPacket() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());
        ByteBufUtils.writeItemStack(out, mainItem);
        ByteBufUtils.writeItemStack(out, offhandItem);
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        if (this.player != null) {
            ItemStack currentItem = ByteBufUtils.readItemStack(inputStream);
            ItemStack offhandItem = ByteBufUtils.readItemStack(inputStream);
            BattlegearUtils.setPlayerCurrentItem(this.player,currentItem);
            BattlegearUtils.setPlayerOffhandItem(this.player,offhandItem);
            Backhand.packetHandler.sendPacketToAll(new BattlegearSyncItemPacket(this.player).generatePacket());
        }
    }
}
