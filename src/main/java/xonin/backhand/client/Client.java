package xonin.backhand.client;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import xonin.backhand.Backhand;
import xonin.backhand.Server;
import xonin.backhand.constants.EnumPacketServer;

import java.io.IOException;

public class Client {

    public static void sendData(EnumPacketServer enu, Object... obs) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            if(!Server.fillBuffer(buffer, enu, obs))
                return;
            Backhand.Channel.sendToServer(new FMLProxyPacket(buffer, "Backhand"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
