package mods.battlegear2.utils;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import xonin.backhand.Backhand;

import java.util.ArrayList;
import java.util.Arrays;

public class BattlegearConfig {
    private static Configuration file;
	public static String[] blacklistedItems = new String[0];
    private static String[] comments = new String[1];

	public static void getConfig(Configuration config) {
        file = config;
        StringBuilder sb;
        String category = Configuration.CATEGORY_GENERAL;

        /*==============================================================================================================
         * GENERAL CONFIGS
         *============================================================================================================*/
        sb = new StringBuilder();
        sb.append("If set to false, an empty offhand will only be rendered when the player is punching with the offhand. False in vanilla.");
        Backhand.OffhandAttack = config.get(category, "Attack with offhand",Backhand.OffhandAttack, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("If set to false, disables offhand actions and rendering if there is no offhand item. False in vanilla.");
        Backhand.EmptyOffhand = config.get(category, "Allow empty offhand",Backhand.EmptyOffhand, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("Determines whether you can break blocks with the offhand or not. False in vanilla.");
        Backhand.OffhandBreakBlocks = config.get(category, "Offhand breaks blocks",Backhand.OffhandBreakBlocks, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("If enabled, arrows in the offhand will be used first when shooting a bow. Compatible with Et-Futurum's tipped arrows! True in vanilla.");
        Backhand.UseOffhandArrows = config.get(category, "Use offhand arrows",Backhand.UseOffhandArrows, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("If enabled, bows can be used in the offhand. True in vanilla.");
        Backhand.UseOffhandBow = config.get(category, "Use offhand bow",Backhand.UseOffhandBow, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("If enabled, an extra offhand slot will be available in the survival inventory screen.");
        Backhand.ExtraInventorySlot = config.get(category, "Extra Inventory Slot",Backhand.ExtraInventorySlot, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("These items will be unable to be swapped into the offhand.\n");
        sb.append("Formatting of an item should be: modid:itemname\n");
        sb.append("These should all be placed on separate lines between the provided \'<\' and \'>\'.");
        blacklistedItems = config.get(category, "Blacklisted items", new String[0], sb.toString()).getStringList();
        Arrays.sort(blacklistedItems);

        ArrayList<Item> items = new ArrayList<>();
        for (String s : blacklistedItems) {
            try {
                String[] split = s.split(":");
                items.add(GameRegistry.findItem(split[0], split[1]));
            } catch (Exception ignored) {}
        }
        Backhand.offhandBlacklist = items.toArray(new Item[0]);

        /*==============================================================================================================
         * RENDERING CONFIGS
         *============================================================================================================*/
        category = "Rendering";
        config.addCustomCategoryComment(category, "This category is client side, you don't have to sync its values with server in multiplayer.");
        sb = new StringBuilder();
        sb.append("If set to false, an empty offhand will only be rendered when the player is punching with the offhand.");
        comments[0] = sb.toString();
        Backhand.RenderEmptyOffhandAtRest = config.get(category, "Render empty offhand at rest",Backhand.RenderEmptyOffhandAtRest, comments[0]).getBoolean();

        file.save();
    }

    public static void refreshConfig(){
        try{
            file.get("Rendering", "Render empty offhand at rest", new String[0], comments[0]).set(Backhand.RenderEmptyOffhandAtRest);
            file.save();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
