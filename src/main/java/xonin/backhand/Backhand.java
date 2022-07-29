package xonin.backhand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.packet.BattlegearPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.config.ConfigLoader;
import noppes.npcs.config.ConfigProp;

import java.io.File;

@Mod(modid = "backhand", name = "Backhand", version = "1.0")
public class Backhand {
    public static Backhand Instance;
    public static ConfigLoader Config;

    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy",
                serverSide = "xonin.backhand.CommonProxy")
    public static CommonProxy proxy;
    public static BattlegearPacketHandler packetHandler;

    @ConfigProp(info="If an extra slot is not made for the offhand, this is the index of where\n" +
                     "the offhand item should go in your inventory.")
    public static int OffhandInventorySlot = 9;

    public Backhand() {
        Instance = this;
    }

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent ev) {
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Backhand");
        ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("BackhandPlayer");

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

        MinecraftForge.EVENT_BUS.register(BattlemodeHookContainerClass.INSTANCE);
        FMLCommonHandler.instance().bus().register(BattlemodeHookContainerClass.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        packetHandler = new BattlegearPacketHandler();
        packetHandler.register();
    }

    public static MinecraftServer getServer(){
        return MinecraftServer.getServer();
    }
}