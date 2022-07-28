package xonin.backhand.client.renderer;

import mods.battlegear2.api.core.BattlegearUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.*;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import xonin.backhand.BackhandUtilPlayer;
import xonin.backhand.client.ClientEventHandler;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

public class ItemRendererOffhand extends ItemRenderer {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

    /** A reference to the Minecraft object. */
    private Minecraft mc;
    private ItemStack itemToRender;
    /** How far the current item has been equipped (0 disequipped and 1 fully up) */
    private float equippedProgress;
    private float prevEquippedProgress;
    private RenderBlocks renderBlocksIr = new RenderBlocks();
    /** The index of the currently held item (0-8, or -1 if not yet updated) */
    private int equippedItemSlot = -1;

    public ItemRendererOffhand(Minecraft mc) {
        super(mc);
        this.mc = mc;
    }

    public void renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_)
    {
        this.renderItem(p_78443_1_, p_78443_2_, p_78443_3_, EQUIPPED);
    }

    public void renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, IItemRenderer.ItemRenderType type)
    {
        GL11.glPushMatrix();
        TextureManager texturemanager = this.mc.getTextureManager();
        Item item = p_78443_2_.getItem();
        Block block = Block.getBlockFromItem(item);

        if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0)
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(p_78443_2_, type);
        if (customRenderer != null)
        {
            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            ForgeHooksClient.renderEquippedItem(type, customRenderer, renderBlocksIr, p_78443_1_, p_78443_2_);
        }
        else
        if (p_78443_2_.getItemSpriteNumber() == 0 && item instanceof ItemBlock && RenderBlocks.renderItemIn3d(block.getRenderType()))
        {
            texturemanager.bindTexture(texturemanager.getResourceLocation(0));

            if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0)
            {
                GL11.glDepthMask(false);
                this.renderBlocksIr.renderBlockAsItem(block, p_78443_2_.getItemDamage(), 1.0F);
                GL11.glDepthMask(true);
            }
            else
            {
                this.renderBlocksIr.renderBlockAsItem(block, p_78443_2_.getItemDamage(), 1.0F);
            }
        }
        else
        {
            IIcon iicon = p_78443_1_.getItemIcon(p_78443_2_, p_78443_3_);

            if (iicon == null)
            {
                GL11.glPopMatrix();
                return;
            }

            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            TextureUtil.func_152777_a(false, false, 1.0F);
            Tessellator tessellator = Tessellator.instance;
            float f = iicon.getMinU();
            float f1 = iicon.getMaxU();
            float f2 = iicon.getMinV();
            float f3 = iicon.getMaxV();
            float f4 = 0.0F;
            float f5 = 0.3F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslatef(-f4, -f5, 0.0F);
            float f6 = 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);

            if (p_78443_2_.hasEffect(p_78443_3_))
            {
                GL11.glDepthFunc(GL11.GL_EQUAL);
                GL11.glDisable(GL11.GL_LIGHTING);
                texturemanager.bindTexture(RES_ITEM_GLINT);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(768, 1, 1, 0);
                float f7 = 0.76F;
                GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glPushMatrix();
                float f8 = 0.125F;
                GL11.glScalef(f8, f8, f8);
                float f9 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
                GL11.glTranslatef(f9, 0.0F, 0.0F);
                GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
                renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                GL11.glScalef(f8, f8, f8);
                f9 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
                GL11.glTranslatef(-f9, 0.0F, 0.0F);
                GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            texturemanager.bindTexture(texturemanager.getResourceLocation(p_78443_2_.getItemSpriteNumber()));
            TextureUtil.func_147945_b();
        }

        if (p_78443_2_ != null && block != null && block.getRenderBlockPass() != 0)
        {
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glPopMatrix();
    }

    /**
     * Renders an item held in hand as a 2D texture with thickness
     */
    public static void renderItemIn2D(Tessellator p_78439_0_, float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, int p_78439_5_, int p_78439_6_, float p_78439_7_)
    {
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, 0.0F, 1.0F);
        p_78439_0_.addVertexWithUV(0.0D, 0.0D, 0.0D, (double)p_78439_1_, (double)p_78439_4_);
        p_78439_0_.addVertexWithUV(1.0D, 0.0D, 0.0D, (double)p_78439_3_, (double)p_78439_4_);
        p_78439_0_.addVertexWithUV(1.0D, 1.0D, 0.0D, (double)p_78439_3_, (double)p_78439_2_);
        p_78439_0_.addVertexWithUV(0.0D, 1.0D, 0.0D, (double)p_78439_1_, (double)p_78439_2_);
        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, 0.0F, -1.0F);
        p_78439_0_.addVertexWithUV(0.0D, 1.0D, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)p_78439_2_);
        p_78439_0_.addVertexWithUV(1.0D, 1.0D, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)p_78439_2_);
        p_78439_0_.addVertexWithUV(1.0D, 0.0D, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)p_78439_4_);
        p_78439_0_.addVertexWithUV(0.0D, 0.0D, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)p_78439_4_);
        p_78439_0_.draw();
        float f5 = 0.5F * (p_78439_1_ - p_78439_3_) / (float)p_78439_5_;
        float f6 = 0.5F * (p_78439_4_ - p_78439_2_) / (float)p_78439_6_;
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(-1.0F, 0.0F, 0.0F);
        int k;
        float f7;
        float f8;

        for (k = 0; k < p_78439_5_; ++k)
        {
            f7 = (float)k / (float)p_78439_5_;
            f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
            p_78439_0_.addVertexWithUV((double)f7, 0.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_4_);
            p_78439_0_.addVertexWithUV((double)f7, 0.0D, 0.0D, (double)f8, (double)p_78439_4_);
            p_78439_0_.addVertexWithUV((double)f7, 1.0D, 0.0D, (double)f8, (double)p_78439_2_);
            p_78439_0_.addVertexWithUV((double)f7, 1.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_2_);
        }

        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(1.0F, 0.0F, 0.0F);
        float f9;

        for (k = 0; k < p_78439_5_; ++k)
        {
            f7 = (float)k / (float)p_78439_5_;
            f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
            f9 = f7 + 1.0F / (float)p_78439_5_;
            p_78439_0_.addVertexWithUV((double)f9, 1.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_2_);
            p_78439_0_.addVertexWithUV((double)f9, 1.0D, 0.0D, (double)f8, (double)p_78439_2_);
            p_78439_0_.addVertexWithUV((double)f9, 0.0D, 0.0D, (double)f8, (double)p_78439_4_);
            p_78439_0_.addVertexWithUV((double)f9, 0.0D, (double)(0.0F - p_78439_7_), (double)f8, (double)p_78439_4_);
        }

        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, 1.0F, 0.0F);

        for (k = 0; k < p_78439_6_; ++k)
        {
            f7 = (float)k / (float)p_78439_6_;
            f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
            f9 = f7 + 1.0F / (float)p_78439_6_;
            p_78439_0_.addVertexWithUV(0.0D, (double)f9, 0.0D, (double)p_78439_1_, (double)f8);
            p_78439_0_.addVertexWithUV(1.0D, (double)f9, 0.0D, (double)p_78439_3_, (double)f8);
            p_78439_0_.addVertexWithUV(1.0D, (double)f9, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)f8);
            p_78439_0_.addVertexWithUV(0.0D, (double)f9, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)f8);
        }

        p_78439_0_.draw();
        p_78439_0_.startDrawingQuads();
        p_78439_0_.setNormal(0.0F, -1.0F, 0.0F);

        for (k = 0; k < p_78439_6_; ++k)
        {
            f7 = (float)k / (float)p_78439_6_;
            f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
            p_78439_0_.addVertexWithUV(1.0D, (double)f7, 0.0D, (double)p_78439_3_, (double)f8);
            p_78439_0_.addVertexWithUV(0.0D, (double)f7, 0.0D, (double)p_78439_1_, (double)f8);
            p_78439_0_.addVertexWithUV(0.0D, (double)f7, (double)(0.0F - p_78439_7_), (double)p_78439_1_, (double)f8);
            p_78439_0_.addVertexWithUV(1.0D, (double)f7, (double)(0.0F - p_78439_7_), (double)p_78439_3_, (double)f8);
        }

        p_78439_0_.draw();
    }

    public void renderOffhandItem(float frame) {
        float progress = this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * frame;

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

        ItemStack offhandItemStack = itemToRender;
        if (offhandItemStack != null)
        {
            int l = offhandItemStack.getItem().getColorFromItemStack(offhandItemStack, 0);
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
        RenderOffhandPlayer var26 = ClientEventHandler.renderOffhandPlayer;
        var7 = 0.8F;
        if (itemToRender != null) {
            {
                GL11.glPushMatrix();

                if (player.getItemInUseCount() > 0) {
                    EnumAction action = itemToRender.getItemUseAction();

                    if (action == EnumAction.eat || action == EnumAction.drink) {
                        var21 = (float) player.getItemInUseCount() - frame + 1.0F;
                        var10 = 1.0F - var21 / (float) itemToRender.getMaxItemUseDuration();
                        var11 = 1.0F - var10;
                        var11 = var11 * var11 * var11;
                        var11 = var11 * var11 * var11;
                        var11 = var11 * var11 * var11;
                        var12 = 1.0F - var11;
                        GL11.glTranslatef(0.0F, MathHelper.abs(MathHelper.cos(var21 / 4.0F * (float) Math.PI) * 0.1F) * (float) ((double) var10 > 0.2D ? 1 : 0), 0.0F);
                        GL11.glTranslatef(var12 * 0.1F, -var12 * 0.1F, 0.0F);
                        GL11.glRotatef(var12 * 2.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(var12 * 5.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(var12 * 3.0F, 0.0F, 0.0F, 1.0F);
                    }
                } else {
                    //var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
                    var20 = player.getSwingProgress(frame);
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
                //var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
                var20 = player.getSwingProgress(frame);

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

                if (player.getItemInUseCount() > 0) {
                    EnumAction action = itemToRender.getItemUseAction();

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
                        var13 = (float) itemToRender.getMaxItemUseDuration() - ((float) player.getItemInUseCount() - frame + 1.0F);
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

                if (itemToRender.getItem().shouldRotateAroundWhenRendering()) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }
                this.renderItem(player, itemToRender, 0);
                if (itemToRender.getItem().requiresMultipleRenderPasses()) {
                    for (int x = 1; x < itemToRender.getItem().getRenderPasses(itemToRender.getItemDamage()); x++) {
                        int k1 = itemToRender.getItem().getColorFromItemStack(offhandItemStack, x);
                        float f10 = (float)(k1 >> 16 & 255) / 255.0F;
                        float f11 = (float)(k1 >> 8 & 255) / 255.0F;
                        float f12 = (float)(k1 & 255) / 255.0F;
                        GL11.glColor4f(f10, f11, f12, 1.0F);
                        this.renderItem(player, itemToRender, x, EQUIPPED_FIRST_PERSON);
                    }
                }

                GL11.glPopMatrix();
            }
        } else if (!player.isInvisible()) {
            GL11.glPushMatrix();

            GL11.glScalef(-1.0F, 1.0F, 1.0F);

            //var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
            var20 = player.getSwingProgress(frame);
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
            var26.renderFirstPersonArm(mc.thePlayer);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }

    public void renderOffhandItemIn3rdPerson(EntityPlayer par1EntityPlayer, ModelBiped modelBipedMain, float frame) {
        ItemStack var21 = this.itemToRender;
        if (var21 != null) {
            float var7;
            GL11.glPushMatrix();
            modelBipedMain.bipedLeftArm.postRender(0.0625F);
            GL11.glTranslatef(0.0625F, 0.4375F, 0.0625F);

            if (par1EntityPlayer.fishEntity != null) {
                var21 = new ItemStack(Items.stick);
            }

            EnumAction var23 = null;

            if (par1EntityPlayer.getItemInUseCount() > 0) {
                var23 = var21.getItemUseAction();
            }

            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(var21, EQUIPPED);
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(EQUIPPED, var21, BLOCK_3D));

            {
                if (var21.getItem() instanceof ItemBlock && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(var21.getItem()).getRenderType()))) {
                    var7 = 0.5F;
                    GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                    var7 *= 0.75F;
                    GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(-var7, -var7, var7);
                } else if (BattlegearUtils.isBow(var21.getItem())) {
                    var7 = 0.625F;
                    GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                    GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(var7, -var7, var7);
                    GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                } else if (var21.getItem().isFull3D()) {
                    var7 = 0.625F;

                    if (var21.getItem().shouldRotateAroundWhenRendering()) {
                        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                    }

                    if (par1EntityPlayer.getItemInUseCount() > 0 && var23 == EnumAction.block) {
                        GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
                    }

                    GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                    GL11.glScalef(var7, -var7, var7);
                    GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                } else {
                    var7 = 0.375F;
                    GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                    GL11.glScalef(var7, var7, var7);
                    GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
                }

                if (var21.getItem().requiresMultipleRenderPasses()) {
                    for (int var27 = 0; var27 < var21.getItem().getRenderPasses(var21.getItemDamage()); ++var27) {
                        applyColorFromItemStack(var21, var27);
                        RenderManager.instance.itemRenderer.renderItem(par1EntityPlayer, var21, var27);
                    }
                } else {
                    applyColorFromItemStack(var21, 0);
                    RenderManager.instance.itemRenderer.renderItem(par1EntityPlayer, var21, 0);
                }
            }
            GL11.glPopMatrix();
        }
    }

    public void applyColorFromItemStack(ItemStack itemStack, int pass){
        int col = itemStack.getItem().getColorFromItemStack(itemStack, pass);
        float r = (float) (col >> 16 & 255) / 255.0F;
        float g = (float) (col >> 8 & 255) / 255.0F;
        float b = (float) (col & 255) / 255.0F;
        GL11.glColor4f(r, g, b, 1.0F);
    }

    public void updateEquippedItem()
    {
        this.prevEquippedProgress = this.equippedProgress;
        EntityClientPlayerMP player = this.mc.thePlayer;
        ItemStack itemstack = BackhandUtilPlayer.getOffhandItem(player);
        boolean flag = this.equippedItemSlot == player.inventory.currentItem && itemstack == this.itemToRender;

        if (this.itemToRender == null && itemstack == null)
        {
            flag = true;
        }

        if (itemstack != null && this.itemToRender != null && itemstack != this.itemToRender && itemstack.getItem() == this.itemToRender.getItem() && itemstack.getItemDamage() == this.itemToRender.getItemDamage())
        {
            this.itemToRender = itemstack;
            flag = true;
        }

        float f = 0.4F;
        float f1 = flag ? 1.0F : 0.0F;
        float f2 = f1 - this.equippedProgress;

        if (f2 < -f)
        {
            f2 = -f;
        }

        if (f2 > f)
        {
            f2 = f;
        }

        this.equippedProgress += f2/2.0F;

        if (this.equippedProgress < 0.1F)
        {
            this.itemToRender = itemstack;
            this.equippedItemSlot = player.inventory.currentItem;
        }
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress()
    {
        this.equippedProgress = 0.0F;
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress2()
    {
        this.equippedProgress = 0.0F;
    }
}
