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
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;
import xonin.backhand.client.ClientEventHandler;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;

public class RenderOffhandPlayer extends RenderPlayer {
    public ItemRendererOffhand itemRenderer = new ItemRendererOffhand(Minecraft.getMinecraft());
    public float tempRenderPartialTicks;
    private float debugCamFOV;
    private float prevDebugCamFOV;
    private float fovModifierHand;
    private float fovModifierHandPrev;
    private float fovMultiplierTemp;

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

    public void renderHand(float partialTicks, int renderPass) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer entityRenderer = mc.entityRenderer;

        if (entityRenderer.debugViewDirection <= 0)
        {
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            float f1 = 0.07F;

            if (mc.gameSettings.anaglyph)
            {
                GL11.glTranslatef((float)(-(renderPass * 2 - 1)) * f1, 0.0F, 0.0F);
            }

            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, mc.gameSettings.renderDistanceChunks * 16 * 2.0F);

            if (mc.playerController.enableEverythingIsScrewedUpMode())
            {
                float f2 = 0.6666667F;
                GL11.glScalef(1.0F, f2, 1.0F);
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            if (mc.gameSettings.anaglyph)
            {
                GL11.glTranslatef((float)(renderPass * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            GL11.glPushMatrix();
            hurtCameraEffect(partialTicks);

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(partialTicks);
            }

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping() && !mc.gameSettings.hideGUI && !mc.playerController.enableEverythingIsScrewedUpMode())
            {
                itemRenderer.updateEquippedItem();
                entityRenderer.enableLightmap((double)partialTicks);
                this.renderOffhandItem(partialTicks);
                entityRenderer.disableLightmap((double)partialTicks);
            }

            GL11.glPopMatrix();

            if (mc.gameSettings.thirdPersonView == 0 && !mc.renderViewEntity.isPlayerSleeping())
            {
                entityRenderer.itemRenderer.renderOverlays(partialTicks);
                hurtCameraEffect(partialTicks);
            }

            if (mc.gameSettings.viewBobbing)
            {
                setupViewBobbing(partialTicks);
            }
        }
    }

    private float getFOVModifier(float p_78481_1_, boolean p_78481_2_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entityplayer = (EntityLivingBase)mc.renderViewEntity;
        float f1 = 70.0F;

        if (p_78481_2_)
        {
            f1 = mc.gameSettings.fovSetting;
            f1 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * p_78481_1_;
        }

        if (entityplayer.getHealth() <= 0.0F)
        {
            float f2 = (float)entityplayer.deathTime + p_78481_1_;
            f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, entityplayer, p_78481_1_);

        if (block.getMaterial() == Material.water)
        {
            f1 = f1 * 60.0F / 70.0F;
        }

        return f1 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * p_78481_1_;
    }

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

    private void hurtCameraEffect(float p_78482_1_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entitylivingbase = mc.renderViewEntity;
        float f1 = (float)entitylivingbase.hurtTime - p_78482_1_;
        float f2;

        if (entitylivingbase.getHealth() <= 0.0F)
        {
            f2 = (float)entitylivingbase.deathTime + p_78482_1_;
            GL11.glRotatef(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
        }

        if (f1 >= 0.0F)
        {
            f1 /= (float)entitylivingbase.maxHurtTime;
            f1 = MathHelper.sin(f1 * f1 * f1 * f1 * (float)Math.PI);
            f2 = entitylivingbase.attackedAtYaw;
            GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    private void setupViewBobbing(float p_78475_1_)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.renderViewEntity instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)mc.renderViewEntity;
            float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f2 = -(entityplayer.distanceWalkedModified + f1 * p_78475_1_);
            float f3 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * p_78475_1_;
            float f4 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * p_78475_1_;
            GL11.glTranslatef(MathHelper.sin(f2 * (float)Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float)Math.PI) * f3), 0.0F);
            GL11.glRotatef(MathHelper.sin(f2 * (float)Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(Math.abs(MathHelper.cos(f2 * (float)Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(f4, 1.0F, 0.0F, 0.0F);
        }
    }

    public void renderOffhandItem(float frame) {
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
        float f5;
        float f6;
        float f7;

        ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
        if (offhandItem != null)
        {
            int l = offhandItem.getItem().getColorFromItemStack(offhandItem, 0);
            f5 = (float)(l >> 16 & 255) / 255.0F;
            f6 = (float)(l >> 8 & 255) / 255.0F;
            f7 = (float)(l & 255) / 255.0F;
            GL11.glColor4f(f5, f6, f7, 1.0F);
        }
        else
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        float var11;
        float var12;
        float var13;
        var7 = 0.8F;
        if (offhandItem != null) {
            {
                GL11.glPushMatrix();

                if (player.getItemInUseCount() > 0 && player.getItemInUse() == offhandItem) {
                    EnumAction action = offhandItem.getItemUseAction();

                    if (action == EnumAction.eat || action == EnumAction.drink) {
                        GL11.glTranslatef(-0.7F * var7, -0.65F * var7 - (1.0F - progress) * 0.6F, -0.9F * var7);

                        var21 = (float) player.getItemInUseCount() - frame + 1.0F;
                        var10 = 1.0F - var21 / (float) offhandItem.getMaxItemUseDuration();
                        var11 = 1.0F - var10;
                        var11 = var11 * var11 * var11;
                        var11 = var11 * var11 * var11;
                        var11 = var11 * var11 * var11;
                        var12 = 1.0F - var11;
                        GL11.glTranslatef(0.0F, MathHelper.abs(MathHelper.cos(1.0F + (var21 / 4.0F * (float) Math.PI)) * 0.1F) * (float) ((double) var10 > 0.15D ? 1 : 0), 0.0F);
                        GL11.glTranslatef(var12 * 0.7F, -var12 * 0.025F, 0.0F);
                        GL11.glRotatef(-var12 * 65.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(var12 * 30.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(-var12 * 20.0F, 0.0F, 0.0F, 1.0F);
                        var11 = 0.4F;
                        GL11.glScalef(var11, var11, var11);
                        itemRenderer.renderItem(player, offhandItem, 0);
                        if (offhandItem.getItem().requiresMultipleRenderPasses()) {
                            for (int x = 1; x < offhandItem.getItem().getRenderPasses(offhandItem.getItemDamage()); x++) {
                                int k1 = offhandItem.getItem().getColorFromItemStack(offhandItem, x);
                                float f10 = (float)(k1 >> 16 & 255) / 255.0F;
                                float f11 = (float)(k1 >> 8 & 255) / 255.0F;
                                float f12 = (float)(k1 & 255) / 255.0F;
                                GL11.glColor4f(f10, f11, f12, 1.0F);
                                itemRenderer.renderItem(player, offhandItem, x, EQUIPPED_FIRST_PERSON);
                            }
                        }
                        GL11.glPopMatrix();
                        return;
                    }
                } else {
                    var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
                    var21 = MathHelper.sin(var20 * (float) Math.PI);
                    var10 = MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI);
                    //Flip the (x direction)
                    GL11.glTranslatef(var10 * 0.4F, MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI * 2.0F) * 0.2F, -var21 * 0.2F);
                }
                //Translate x in the opposite direction
                GL11.glTranslatef(-0.7F * var7, -0.65F * var7 - (1.0F - progress) * 0.6F, -0.9F * var7);

                //Rotate y in the opposite direction
                GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);

                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);

                var21 = MathHelper.sin(var20 * var20 * (float) Math.PI);
                var10 = MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI);

                GL11.glRotatef(-var21 * 20.0F, 0.0F, 1.0F, 0.0F);
                //Rotate z in the opposite direction
                GL11.glRotatef(var10 * 20.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-var10 * 80.0F, 1.0F, 0.0F, 0.0F);

                //Rotate y back to original position + 45
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);

                var11 = 0.4F;
                GL11.glScalef(var11, var11, var11);
                float var14;
                float var15;

                if (player.getItemInUseCount() > 0 && player.getItemInUse() == offhandItem) {
                    EnumAction action = offhandItem.getItemUseAction();

                    if (action == EnumAction.block) {
                        GL11.glTranslatef(0.0F, 0.2F, 0.0F);
                        GL11.glRotatef(30.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
                    } else if (action == EnumAction.bow) {
                        GL11.glRotatef(-18.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glRotatef(-12.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(-8.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glTranslatef(-0.9F, 0.2F, 0.0F);
                        var13 = (float) offhandItem.getMaxItemUseDuration() - ((float) player.getItemInUseCount() - frame + 1.0F);
                        var14 = var13 / 20.0F;
                        var14 = (var14 * var14 + var14 * 2.0F) / 3.0F;

                        if (var14 > 1.0F) {
                            var14 = 1.0F;
                        }

                        if (var14 > 0.1F) {
                            GL11.glTranslatef(0.0F, MathHelper.sin((var13 - 0.1F) * 1.3F) * 0.01F * (var14 - 0.1F), 0.0F);
                        }

                        GL11.glTranslatef(0.0F, 0.0F, var14 * 0.1F);
                        GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                        var15 = 1.0F + var14 * 0.2F;
                        GL11.glScalef(1.0F, 1.0F, var15);
                        GL11.glTranslatef(0.0F, -0.5F, 0.0F);
                        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
                    }
                }

                if (offhandItem.getItem().shouldRotateAroundWhenRendering()) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }
                if (offhandItem.getItem() != null && offhandItem.getItem().getUnlocalizedName() != null && offhandItem.getItem().getUnlocalizedName().toLowerCase().endsWith("arrow")) {
                    GL11.glTranslatef(-0.55F,0.0F,0.5F);
                    GL11.glRotatef(-90,1,0,1);
                }
                itemRenderer.renderItem(player, offhandItem, 0);
                if (offhandItem.getItem().requiresMultipleRenderPasses()) {
                    for (int x = 1; x < offhandItem.getItem().getRenderPasses(offhandItem.getItemDamage()); x++) {
                        int k1 = offhandItem.getItem().getColorFromItemStack(offhandItem, x);
                        float f10 = (float)(k1 >> 16 & 255) / 255.0F;
                        float f11 = (float)(k1 >> 8 & 255) / 255.0F;
                        float f12 = (float)(k1 & 255) / 255.0F;
                        GL11.glColor4f(f10, f11, f12, 1.0F);
                        itemRenderer.renderItem(player, offhandItem, x, EQUIPPED_FIRST_PERSON);
                    }
                }

                GL11.glPopMatrix();
            }
        } else if (!player.isInvisible()) {
            GL11.glPushMatrix();

            GL11.glScalef(-1.0F, 1.0F, 1.0F);

            var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
            //var20 = player.getSwingProgress(frame);
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
            this.renderFirstPersonArm(mc.thePlayer);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }

    public void renderFirstPersonArm(EntityPlayer player) {
        Render render = RenderManager.instance.getEntityRenderObject(player);
        RenderPlayer renderplayer = (RenderPlayer)render;

        GL11.glPushMatrix();
        renderplayer.modelBipedMain.onGround = 0.0F;
        renderplayer.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);

        ModelRenderer rightArm = renderplayer.modelBipedMain.bipedRightArm;
        ModelRenderer leftArm = renderplayer.modelBipedMain.bipedLeftArm;
        float rotationPointX = leftArm.rotationPointX;
        float rotationPointY = leftArm.rotationPointY;
        float rotationPointZ = leftArm.rotationPointZ;
        float offsetX = leftArm.offsetX;
        float offsetY = leftArm.offsetY;
        float offsetZ = leftArm.offsetZ;

        leftArm.setRotationPoint(rightArm.rotationPointX, rightArm.rotationPointY, rightArm.rotationPointZ);
        leftArm.offsetX = rightArm.offsetX;
        leftArm.offsetY = rightArm.offsetY - 0.04F;
        leftArm.offsetZ = rightArm.offsetZ;

        leftArm.rotateAngleX = -0.1F;
        leftArm.rotateAngleZ = 0.1F;
        leftArm.rotateAngleY = 90.95F;
        leftArm.render(0.0625F);
        leftArm.rotateAngleX = 0F;
        leftArm.rotateAngleZ = 0;
        leftArm.rotateAngleY = 0;

        leftArm.setRotationPoint(rotationPointX, rotationPointY, rotationPointZ);
        leftArm.offsetX = offsetX;
        leftArm.offsetY = offsetY;
        leftArm.offsetZ = offsetZ;

        GL11.glPopMatrix();
    }
}
