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

	public static void getConfig(Configuration config) {
        file = config;
        StringBuilder sb;
        String category = Configuration.CATEGORY_GENERAL;

        /*==============================================================================================================
         * GENERAL CONFIGS
         *============================================================================================================*/
        sb = new StringBuilder();
        sb.append("If set to false, an empty offhand will only be rendered when the player is punching with the offhand.");
        Backhand.OffhandAttack = config.get(category, "Attack with offhand",Backhand.OffhandAttack, sb.toString()).getBoolean();

        sb = new StringBuilder();
        sb.append("If set to false, disables offhand actions and rendering if there is no offhand item.");
        Backhand.EmptyOffhand = config.get(category, "Allow empty offhand",Backhand.EmptyOffhand, sb.toString()).getBoolean();

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
        Backhand.RenderEmptyOffhandAtRest = config.get(category, "Render empty offhand at rest",Backhand.RenderEmptyOffhandAtRest, sb.toString()).getBoolean();

        file.save();
    }
}