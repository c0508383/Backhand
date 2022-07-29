package xonin.backhand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.utils.EnumBGAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy implements IGuiHandler {

    public void load() {

    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        /*if(ID > EnumGuiType.values().length)
            return null;
        EnumGuiType gui = EnumGuiType.values()[ID];
        return getContainer(gui, player, x, y, z, npc);*/
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void sendAnimationPacket(EnumBGAnimations animation, EntityPlayer entityPlayer) {}

    // Should not be called on the server anyway
    public boolean isRightClickHeld() {
        return false;
    }

    // Should not be called on the server anyway
    public int getLeftClickCounter() {
        return 0;
    }

    // Should not be called on the server anyway
    public void setLeftClickCounter(int i) {}

    // Should not be called on the server anyway
    public boolean isLeftClickHeld() {
        return false;
    }
}
