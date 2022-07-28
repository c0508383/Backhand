package xonin.backhand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.config.ConfigLoader;
import noppes.npcs.config.ConfigProp;

import java.io.File;

@Mod(modid = Backhand.MODID, useMetadata = true, version = Backhand.VERSION, name = "Backhand")
public class Backhand {
    public static final String MODID = "backhand";
    public static final String VERSION = "1.0";

    @Mod.Instance("backhand")
    public static Backhand Instance;
    public static ConfigLoader Config;

    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy",
                serverSide = "xonin.backhand.CommonProxy",
                modId = "backhand")
    public static CommonProxy proxy;

    @ConfigProp(info="If an extra slot is not made for the offhand, this is the index of where\n" +
                     "the offhand item should go in your inventory.")
    public static int OffhandInventorySlot = 9;

    public Backhand() {
        Instance = this;
    }

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent ev) {
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCs");
        ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCsPlayer");

        MinecraftServer server = MinecraftServer.getServer();
        String dir = "";
        if (server != null) {
            dir = new File(".").getAbsolutePath();
        } else {
            dir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
        }

        Config = new ConfigLoader(this.getClass(), new File(dir, "config"), "Backhand");
        Config.loadConfig();

        if (OffhandInventorySlot < 0)
            OffhandInventorySlot = 0;
        if (OffhandInventorySlot > 39)
            OffhandInventorySlot = 39;

        proxy.load();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());
    }

    public static MinecraftServer getServer(){
        return MinecraftServer.getServer();
    }
}