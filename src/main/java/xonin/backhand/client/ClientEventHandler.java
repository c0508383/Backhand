package xonin.backhand.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.BattlegearUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import xonin.backhand.client.renderer.RenderOffhandPlayer;

public class ClientEventHandler {
    public static final RenderOffhandPlayer renderOffhandPlayer = new RenderOffhandPlayer();
    public static boolean cancelone = false;
    public static int delay;

    @SubscribeEvent
    public void renderHotbarOverlay(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        renderHotbar(mc.ingameGUI, event.resolution.getScaledWidth(), event.resolution.getScaledHeight(), event.partialTicks);
    }

    protected void renderHotbar(GuiIngame gui, int width, int height, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack itemstack = BattlegearUtils.getOffhandItem(mc.thePlayer);
        if (itemstack == null) {
            return;
        }

        mc.mcProfiler.startSection("actionBar");

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(new ResourceLocation("textures/gui/widgets.png"));

        gui.drawTexturedModalRect(width / 2 - 125, height - 22, 0, 0, 11, 22);
        gui.drawTexturedModalRect(width / 2 - 125 + 11, height - 22, 182 - 11, 0, 11, 22);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();

        int x = width / 2 - 122;
        int z = height - 16 - 3;
        renderOffhandInventorySlot(x, z, partialTicks);

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        mc.mcProfiler.endSection();
    }

    protected void renderOffhandInventorySlot(int p_73832_2_, int p_73832_3_, float p_73832_4_) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack itemstack = BattlegearUtils.getOffhandItem(mc.thePlayer);

        if (itemstack != null)
        {
            float f1 = itemstack.animationsToGo - p_73832_4_;

            if (f1 > 0.0F)
            {
                GL11.glPushMatrix();
                float f2 = 1.0F + f1 / 5.0F;
                GL11.glTranslatef(p_73832_2_ + 8, p_73832_3_ + 12, 0.0F);
                GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((-(p_73832_2_ + 8)), (-(p_73832_3_ + 12)), 0.0F);
            }

            RenderItem.getInstance().renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemstack, p_73832_2_, p_73832_3_);

            if (f1 > 0.0F)
            {
                GL11.glPopMatrix();
            }

            RenderItem.getInstance().renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemstack, p_73832_2_, p_73832_3_);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null && !mc.isGamePaused() && event.phase == TickEvent.Phase.END) {
            renderOffhandPlayer.itemRenderer.updateEquippedItem();
            renderOffhandPlayer.updateFovModifierHand();
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        GL11.glPushMatrix();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        renderOffhandPlayer.renderHand(event.partialTicks, event.renderPass);
        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void render3rdPersonBattlemode(RenderPlayerEvent.Specials.Post event) {
        GL11.glPushMatrix();
        ModelBiped biped = (ModelBiped) event.renderer.modelBipedMain;
        renderOffhandPlayer.itemRenderer.renderOffhandItemIn3rdPerson(event.entityPlayer, biped, event.partialRenderTick);
        GL11.glPopMatrix();
    }
}
