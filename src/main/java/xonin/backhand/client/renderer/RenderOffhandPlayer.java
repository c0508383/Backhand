package xonin.backhand.client.renderer;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;
import xonin.backhand.client.ClientEventHandler;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;

public class RenderOffhandPlayer extends RenderPlayer {
    public static ItemRendererOffhand itemRenderer = new ItemRendererOffhand(Minecraft.getMinecraft());
    private float fovModifierHand;
    private float fovModifierHandPrev;
    private float fovMultiplierTemp;
    private RenderBlocks renderBlocksIr = new RenderBlocks();

    public RenderOffhandPlayer() {
        super();
        this.modelBipedMain = (ModelBiped)this.mainModel;
        this.modelArmorChestplate = new ModelBiped(1.0F);
        this.modelArmor = new ModelBiped(0.5F);
        this.setRenderManager(RenderManager.instance);
    }

    @Override
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        EntityPlayer player = (EntityPlayer) p_77036_1_;

        if (!player.isInvisible()) {
            this.modelBipedMain.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
        }
    }

    protected int shouldRenderPass(AbstractClientPlayer p_77032_1_, int p_77032_2_, float p_77032_3_)
    {
        return -1;
    }

    protected void passSpecialRender(EntityLivingBase p_77033_1_, double p_77033_2_, double p_77033_4_, double p_77033_6_) {}

    protected void func_96449_a(AbstractClientPlayer p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_) {}

    public void updateFovModifierHand()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayerSP)
        {
            EntityPlayerSP entityplayersp = (EntityPlayerSP)mc.renderViewEntity;
            this.fovMultiplierTemp = entityplayersp.getFOVMultiplier();
        }
        else
        {
            this.fovMultiplierTemp = mc.thePlayer.getFOVMultiplier();
        }
        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (this.fovMultiplierTemp - this.fovModifierHand) * 0.5F;

        if (this.fovModifierHand > 1.5F)
        {
            this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F)
        {
            this.fovModifierHand = 0.1F;
        }
    }

    public void renderOffhandItem(ItemRenderer otherItemRenderer, float frame)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        ItemStack itemstack = BattlegearUtils.getOffhandItem(mc.thePlayer);
        if (itemstack == null) {
            GL11.glCullFace(GL11.GL_BACK);
            this.renderFirstPersonLeftArm(frame);
            GL11.glCullFace(GL11.GL_FRONT);
            return;
        }

        GL11.glPushMatrix();
        GL11.glScalef(-1,1,1);

        ItemStack itemToRender = otherItemRenderer.itemToRender;
        float equippedProgress = otherItemRenderer.equippedProgress;
        float prevEquippedProgress = otherItemRenderer.prevEquippedProgress;

        otherItemRenderer.itemToRender = BattlegearUtils.getOffhandItem(player);
        otherItemRenderer.equippedProgress = itemRenderer.equippedProgress;
        otherItemRenderer.prevEquippedProgress = itemRenderer.prevEquippedProgress;

        otherItemRenderer.renderItemInFirstPerson(frame);

        otherItemRenderer.itemToRender = itemToRender;
        otherItemRenderer.equippedProgress = equippedProgress;
        otherItemRenderer.prevEquippedProgress = prevEquippedProgress;

        GL11.glPopMatrix();
    }

    public void renderFirstPersonLeftArm(float frame) {
        Minecraft mc = Minecraft.getMinecraft();
        float progress = itemRenderer.prevEquippedProgress + (itemRenderer.equippedProgress - itemRenderer.prevEquippedProgress) * frame;

        EntityClientPlayerMP player = mc.thePlayer;

        float rotation = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * frame;
        GL11.glPushMatrix();
        GL11.glRotatef(rotation, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * frame, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        float var6;
        float var7;

        var6 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * frame;
        var7 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * frame;
        GL11.glRotatef((player.rotationPitch - var6) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((player.rotationYaw - var7) * 0.1F, 0.0F, 1.0F, 0.0F);

        int var18 = mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), 0);
        int var8 = var18 % 65536;
        int var9 = var18 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) var8 / 1.0F, (float) var9 / 1.0F);
        float var10;
        float var21;
        float var20;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        var7 = 0.8F;
        if (!player.isInvisible()) {
            GL11.glPushMatrix();

            GL11.glScalef(-1.0F, 1.0F, 1.0F);

            var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
            var21 = MathHelper.sin(var20 * (float) Math.PI);
            var10 = MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI);
            GL11.glTranslatef(-var10 * 0.3F, MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI * 2.0F) * 0.4F, -var21 * 0.4F);
            GL11.glTranslatef(var7 * var7, -0.75F * var7 - (1.0F - progress) * 0.6F, -0.9F * var7);

            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            var21 = MathHelper.sin(var20 * var20 * (float) Math.PI);
            GL11.glRotatef(var10 * 70.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(var21 * 20.0F, 0.0F, 0.0F, 1.0F);

            mc.getTextureManager().bindTexture(player.getLocationSkin());
            GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
            GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

            GL11.glScalef(1.0F, 1.0F, -1.0F);
            GL11.glTranslatef(5.6F, 0.0F, 0.0F);
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            renderFirstPersonLeftArm(mc.thePlayer);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }

    public void renderFirstPersonLeftArm(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        Render render = RenderManager.instance.getEntityRenderObject(player);
        RenderPlayer renderplayer = (RenderPlayer)render;

        GL11.glPushMatrix();
        renderplayer.modelBipedMain.onGround = 0.0F;
        renderplayer.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
        ModelRenderer rightArm = renderplayer.modelBipedMain.bipedRightArm;
        ModelRenderer leftArm = renderplayer.modelBipedMain.bipedLeftArm;

        try {
            Class<?> RenderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
            Class<?> ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");
            Object modelMain = RenderPlayerJBRA.getField("modelMain").get(renderplayer);

            try {
                ModelRenderer bipedRA = (ModelRenderer) ModelBipedBody.getField("field_78112_f").get(modelMain);
                ModelRenderer bipedLA = (ModelRenderer) ModelBipedBody.getField("field_78113_g").get(modelMain);
                ModelBipedBody.getField("field_78112_f").set(modelMain, bipedLA);
                GL11.glRotatef(180, 0, 1, 0);
                RenderPlayerJBRA.getMethod("func_82441_a", EntityPlayer.class).invoke(renderplayer, player);
                renderplayer.modelBipedMain.bipedRightArm = leftArm;
                this.renderCNPCOverlays(player);
                renderplayer.modelBipedMain.bipedRightArm = rightArm;
                ModelBipedBody.getField("field_78112_f").set(modelMain, bipedRA);
            } catch (NoSuchFieldException | NoSuchMethodException e) {
                ModelRenderer bipedRA = (ModelRenderer) ModelBipedBody.getField("bipedRightArm").get(modelMain);
                ModelRenderer bipedLA = (ModelRenderer) ModelBipedBody.getField("bipedLeftArm").get(modelMain);
                ModelBipedBody.getField("bipedRightArm").set(modelMain,bipedLA);
                GL11.glRotatef(180,0,1,0);
                RenderPlayerJBRA.getMethod("renderFirstPersonArm", EntityPlayer.class).invoke(renderplayer, player);
                renderplayer.modelBipedMain.bipedRightArm = leftArm;
                this.renderCNPCOverlays(player);
                renderplayer.modelBipedMain.bipedRightArm = rightArm;
                ModelBipedBody.getField("bipedRightArm").set(modelMain,bipedRA);
            }
        } catch (Exception ignored) {
            renderplayer.modelBipedMain.bipedRightArm = leftArm;
            GL11.glRotatef(180,0,1,0);
            mc.getTextureManager().bindTexture(((AbstractClientPlayer)player).getLocationSkin());
            renderplayer.renderFirstPersonArm(player);
            this.renderCNPCOverlays(player);
            renderplayer.modelBipedMain.bipedRightArm = rightArm;
        }

        GL11.glPopMatrix();
    }

    private void renderCNPCOverlays(EntityPlayer player) {
        try {
            Class<?> RenderCNPCPlayer = Class.forName("noppes.npcs.client.renderer.RenderCNPCPlayer");
            Class<?> ClientEventHandler = Class.forName("noppes.npcs.client.ClientEventHandler");
            Object renderCNPCPlayer = ClientEventHandler.getField("renderCNPCPlayer").get(null);

            ModelRenderer rightArm = ((RenderPlayer)renderCNPCPlayer).modelBipedMain.bipedRightArm;
            ((RenderPlayer)renderCNPCPlayer).modelBipedMain.bipedRightArm = ((RenderPlayer)renderCNPCPlayer).modelBipedMain.bipedLeftArm;
            RenderCNPCPlayer.getMethod("renderFirstPersonArmOverlay",EntityPlayer.class).invoke(renderCNPCPlayer,player);
            ((RenderPlayer)renderCNPCPlayer).modelBipedMain.bipedRightArm = rightArm;
        } catch (Exception ignored) {}
    }
}
