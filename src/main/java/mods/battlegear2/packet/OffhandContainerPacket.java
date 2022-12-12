package mods.battlegear2.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.ContainerPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;
import xonin.backhand.Backhand;

public class OffhandContainerPacket extends AbstractMBPacket {
    public static final String packetName = "MB2|SendContainer";
    private String user;
    EntityPlayer player;
    boolean resetContainer;

    public OffhandContainerPacket(EntityPlayer player, boolean resetContainer) {
        this.player = player;
        this.user = player.getCommandSenderName();
        this.resetContainer = resetContainer;
    }

    public OffhandContainerPacket() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());
        out.writeBoolean(this.resetContainer);
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        this.user = ByteBufUtils.readUTF8String(inputStream);
        this.player = player.worldObj.getPlayerEntityByName(user);
        this.resetContainer = inputStream.readBoolean();

        if (this.player != null) {
            if (!this.resetContainer) {
                this.player.inventoryContainer = new ContainerPlayerBattle(player.inventory, !player.worldObj.isRemote, player);
                this.player.openContainer = this.player.inventoryContainer;

                if (player instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) player).addSelfToInternalCraftingInventory();
                }
            } else if (this.player.inventoryContainer instanceof ContainerPlayerBattle) {
                this.player.inventoryContainer = new ContainerPlayer(this.player.inventory, !this.player.worldObj.isRemote, this.player);
                this.player.openContainer = this.player.inventoryContainer;

                if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                    ((EntityPlayerMP) player).addSelfToInternalCraftingInventory();
                    Backhand.packetHandler.sendPacketToPlayer(new OffhandContainerPacket(player, true).generatePacket(), (EntityPlayerMP) player);
                } else {
                    MysteriumPatchesFixesO.disableGUIOpen = false;
                }
            }
        }
    }
}
