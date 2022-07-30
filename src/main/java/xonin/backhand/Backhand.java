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

    @ConfigProp(info="Determines whether you can attack with the offhand, or if it's just used \n" +
                     "for using items like in Vanilla MC.")
    public static boolean OffhandPunch = false;

    @ConfigProp(info="If set to false, disables offhand actions and rendering if there is no offhand item.")
    public static boolean EmptyOffhand = false;

    @ConfigProp(info="Client sided! If set to false, an empty offhand will only be rendered \n" +
            "when the player is punching with the offhand.")
    public static boolean RenderEmptyOffhandAtRest = true;

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