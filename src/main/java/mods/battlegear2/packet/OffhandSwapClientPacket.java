
package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import xonin.backhand.client.ClientTickHandler;

/**
 * User: nerd-boy
 * Date: 26/06/13
 * Time: 1:40 PM
 */
public final class OffhandSwapClientPacket extends AbstractMBPacket {

    public static final String packetName = "MB2|SwapClient";

    public OffhandSwapClientPacket() {
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        ClientTickHandler.allowSwap = true;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
    }
}
