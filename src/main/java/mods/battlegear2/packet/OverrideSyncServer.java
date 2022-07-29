package mods.battlegear2.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;

public class OverrideSyncServer extends AbstractMBPacket {
	public static final String packetName = "MB2|OverrideSync";

	 // The basic, no-argument constructor MUST be included to use the new automated handling
	public OverrideSyncServer() {}

	@Override
	public String getChannel() {
		return packetName;
	}

	@Override
	public void process(ByteBuf inputStream, EntityPlayer player) {
		MysteriumPatchesFixesO.shouldNotOverride = inputStream.readBoolean();
	}

	@Override
	public void write(ByteBuf out) {
		out.writeBoolean(MysteriumPatchesFixesO.shouldNotOverride);
	}
}
