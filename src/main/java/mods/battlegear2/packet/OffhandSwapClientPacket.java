
package mods.battlegear2.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
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
