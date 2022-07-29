package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.BattlemodeHookContainerClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class InteractWithSyncServer implements IMessage {

	private NBTTagCompound data;

	 // The basic, no-argument constructor MUST be included to use the new automated handling
		public InteractWithSyncServer() {}

	 // We need to initialize our data, so provide a suitable constructor:
		public InteractWithSyncServer(EntityPlayer player) {
			data = new NBTTagCompound();
			data.setBoolean("intw", BattlemodeHookContainerClass.interactWith);
		}

		@Override
	 	public void fromBytes(ByteBuf buffer) {
		 	data = ByteBufUtils.readTag(buffer);
	 	}

		 @Override
		 public void toBytes(ByteBuf buffer) {
			 ByteBufUtils.writeTag(buffer, data);
		 }

		 public static class Handler implements IMessageHandler<InteractWithSyncServer, IMessage> {

			 @Override
		     public IMessage onMessage(InteractWithSyncServer message, MessageContext ctx) {
				    BattlemodeHookContainerClass.interactWith = message.data.getBoolean("intw");
					return null;
			 }
		 }
}
