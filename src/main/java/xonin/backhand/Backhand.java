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
import mods.battlegear2.utils.BattlegearConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

@Mod(modid = "backhand", name = "Backhand", version = "1.0.1")
public class Backhand {
    public static Backhand Instance;

    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    @SidedProxy(clientSide = "xonin.backhand.client.ClientProxy",
                serverSide = "xonin.backhand.CommonProxy")
    public static CommonProxy proxy;
    public static BattlegearPacketHandler packetHandler;

    public static boolean OffhandAttack = false;
    public static boolean EmptyOffhand = false;
    public static boolean OffhandBreakBlocks = false;
    public static boolean UseOffhandArrows = true;
    public static boolean UseOffhandBow = true;
    public static Item[] offhandBlacklist;
    public static boolean RenderEmptyOffhandAtRest = true;

    public Backhand() {
        Instance = this;
    }

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent event) {
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Backhand");
        ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("BackhandPlayer");

        MinecraftServer server = MinecraftServer.getServer();
        String dir = "";
        if (server != null) {
            dir = new File(".").getAbsolutePath();
        } else {
            dir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
        }

        BattlegearConfig.getConfig(new Configuration(event.getSuggestedConfigurationFile()));

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

    public static boolean isOffhandBlacklisted(ItemStack stack) {
        if (stack == null)
            return false;

        for (Item item : offhandBlacklist) {
            if (stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }
}