package net.tclproject.mysteriumlib.asm.fixes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import invtweaks.InvTweaksContainerManager;
import invtweaks.InvTweaksContainerSectionManager;
import invtweaks.api.container.ContainerSection;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.ContainerPlayerBattle;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting;
import net.tclproject.mysteriumlib.asm.annotations.Fix;
import net.tclproject.mysteriumlib.asm.annotations.ReturnedValue;
import xonin.backhand.Backhand;
import xonin.backhand.CommonProxy;
import xonin.backhand.client.ClientEventHandler;
import xonin.backhand.client.ClientProxy;
import xonin.backhand.client.renderer.RenderOffhandPlayer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class MysteriumPatchesFixesO {
    /**Dirty hack to prevent random resetting of block removal (why does this even happen?!) when breaking blocks with the offhand.*/
    public static int countToCancel = 0;
    /**If we have hotswapped the breaking item with the one in offhand and should hotswap it back when called next*/
    public static boolean hotSwapped = false;

    @Fix(returnSetting=EnumReturnSetting.ALWAYS)
	public static boolean isPlayer(EntityPlayer p) {
		return false;
	}

    @Fix(insertOnExit = true)
    public static void damageItem(ItemStack itemStack, int p_77972_1_, EntityLivingBase p_77972_2_)
    {
        if (!(p_77972_2_ instanceof EntityPlayer) || itemStack == null)
            return;

        EntityPlayer player = (EntityPlayer) p_77972_2_;
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
        if (offhandItem != null && itemStack == offhandItem && itemStack.stackSize == 0) {
            BattlegearUtils.setPlayerOffhandItem(player,null);
            ForgeEventFactory.onPlayerDestroyItem(player,offhandItem);
        }
    }

    @Fix(insertOnExit = true, returnSetting=EnumReturnSetting.ALWAYS)
    public static EnumAction getItemUseAction(ItemStack itemStack, @ReturnedValue EnumAction returnedAction)
    {
        if (returnedAction != EnumAction.none) {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                EntityPlayer player = ClientEventHandler.renderingPlayer;
                ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
                if (CommonProxy.offhandItemUsed != null && CommonProxy.offhandItemUsed != itemStack
                        && offhandItem != null && BattlegearUtils.checkForRightClickFunctionNoAction(offhandItem) && itemStack != offhandItem) {
                    return EnumAction.none;
                }
            }
        }
        return itemStack.getItem().getItemUseAction(itemStack);
    }

    private static boolean disableMainhandAnimation = false;
    @SideOnly(Side.CLIENT)
    @Fix(insertOnExit = true, returnSetting = EnumReturnSetting.ALWAYS)
    public static IIcon getItemIcon(EntityLivingBase entity, ItemStack p_70620_1_, int p_70620_2_, @ReturnedValue IIcon returnValue)
    {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (p_70620_1_ == player.getCurrentEquippedItem() && player.getCurrentEquippedItem() != null
                    && player.getItemInUse() != null
                    && player.getCurrentEquippedItem().getItem() instanceof ItemBow
                    && player.getCurrentEquippedItem() != player.getItemInUse()) {
                disableMainhandAnimation = true;
            }
        }
        return returnValue;
    }

    @SideOnly(Side.CLIENT)
    @Fix(insertOnExit = true, returnSetting = EnumReturnSetting.ALWAYS)
    public static IIcon getItemIconForUseDuration(ItemBow bow, int p_94599_1_, @ReturnedValue IIcon returnValue)
    {
        if (disableMainhandAnimation) {
            disableMainhandAnimation = false;
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            return bow.getIcon(player.getCurrentEquippedItem(),0, player, player.getItemInUse(),0);
        }
        return returnValue;
    }

    @SideOnly(Side.CLIENT)
    @Fix(returnSetting=EnumReturnSetting.ON_TRUE)
    public static boolean resetBlockRemoving(PlayerControllerMP controller)
    {
        if (countToCancel > 0) {
            countToCancel--;
            return true;
        } else {
            if (MysteriumPatchesFixesO.hotSwapped) {
                Minecraft.getMinecraft().playerController.syncCurrentPlayItem();
                MysteriumPatchesFixesO.hotSwapped = false;
            }
            return false;
        }
    }

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	@SideOnly(Side.CLIENT)
	public static boolean clickBlock(PlayerControllerMP mp, int p_78743_1_, int p_78743_2_, int p_78743_3_, int p_78743_4_)
    {
		if (ClientEventHandler.cancelone) {
			mp.resetBlockRemoving();
			return true;
		}
		return false;
    }

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	@SideOnly(Side.CLIENT)
	public static boolean addBlockHitEffects(EffectRenderer er, int x, int y, int z, MovingObjectPosition target)
    {
		if (ClientEventHandler.cancelone) {
			return true;
		}
		return false;
    }

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	@SideOnly(Side.CLIENT)
	public static boolean onPlayerDamageBlock(PlayerControllerMP mp, int p_78759_1_, int p_78759_2_, int p_78759_3_, int p_78759_4_)
    {
		if (ClientEventHandler.cancelone) {
			mp.resetBlockRemoving();
			return true;
		}
		return false;
    }

	public static float onGround2;

    @Fix(insertOnExit = true)
    @SideOnly(Side.CLIENT)
    public static void renderItemInFirstPerson(ItemRenderer i, float p_78440_1_)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ClientEventHandler.renderingPlayer = player;
        ItemStack offhandItem = BattlegearUtils.getOffhandItem(player);
        if (!Backhand.EmptyOffhand && !Backhand.RenderEmptyOffhandAtRest && offhandItem == null) {
            return;
        }
        if (offhandItem == null && !Backhand.RenderEmptyOffhandAtRest && ((IBattlePlayer)player).getOffSwingProgress(p_78440_1_) == 0) {
            return;
        }

        MysteriumPatchesFixesO.onGround2 = 0;
        RenderOffhandPlayer.itemRenderer.updateEquippedItem();
        ClientEventHandler.renderOffhandPlayer.renderOffhandItem(p_78440_1_);
    }

	@Fix
	@SideOnly(Side.CLIENT)
	public static void doRender(RendererLivingEntity l, EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
		if (p_76986_1_ instanceof EntityPlayer) {
			onGround2 = ((IBattlePlayer)p_76986_1_).getOffSwingProgress(p_76986_9_);
		}
    }

	@Fix(returnSetting=EnumReturnSetting.ALWAYS)
	@SideOnly(Side.CLIENT)
	public static void setRotationAngles(ModelBiped b, float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_)
    {
        b.bipedHead.rotateAngleY = p_78087_4_ / (180F / (float)Math.PI);
        b.bipedHead.rotateAngleX = p_78087_5_ / (180F / (float)Math.PI);
        b.bipedHeadwear.rotateAngleY = b.bipedHead.rotateAngleY;
        b.bipedHeadwear.rotateAngleX = b.bipedHead.rotateAngleX;
        b.bipedRightArm.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float)Math.PI) * 2.0F * p_78087_2_ * 0.5F;
        b.bipedLeftArm.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 2.0F * p_78087_2_ * 0.5F;
        b.bipedRightArm.rotateAngleZ = 0.0F;
        b.bipedLeftArm.rotateAngleZ = 0.0F;
        b.bipedRightLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
        b.bipedLeftLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float)Math.PI) * 1.4F * p_78087_2_;
        b.bipedRightLeg.rotateAngleY = 0.0F;
        b.bipedLeftLeg.rotateAngleY = 0.0F;

        if (b.isRiding)
        {
            b.bipedRightArm.rotateAngleX += -((float)Math.PI / 5F);
            b.bipedLeftArm.rotateAngleX += -((float)Math.PI / 5F);
            b.bipedRightLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            b.bipedLeftLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            b.bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
            b.bipedLeftLeg.rotateAngleY = -((float)Math.PI / 10F);
        }

        if (b.heldItemLeft != 0)
        {
            b.bipedLeftArm.rotateAngleX = b.bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float)b.heldItemLeft;
        }

        if (b.heldItemRight != 0)
        {
            b.bipedRightArm.rotateAngleX = b.bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float)b.heldItemRight;
        }

        b.bipedRightArm.rotateAngleY = 0.0F;
        b.bipedLeftArm.rotateAngleY = 0.0F;
        float f6;
        float f7;

        if (b.onGround > -9990.0F)
        {
            f6 = b.onGround;
            b.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * (float)Math.PI * 2.0F) * 0.2F;
            b.bipedRightArm.rotationPointZ = MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
            b.bipedRightArm.rotationPointX = -MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
            b.bipedLeftArm.rotationPointZ = -MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
            b.bipedLeftArm.rotationPointX = MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
            b.bipedRightArm.rotateAngleY += b.bipedBody.rotateAngleY;
            b.bipedLeftArm.rotateAngleY += b.bipedBody.rotateAngleY;
            b.bipedLeftArm.rotateAngleX += b.bipedBody.rotateAngleY;
            f6 = 1.0F - b.onGround;
            f6 *= f6;
            f6 *= f6;
            f6 = 1.0F - f6;
            f7 = MathHelper.sin(f6 * (float)Math.PI);
            float f8 = MathHelper.sin(b.onGround * (float)Math.PI) * -(b.bipedHead.rotateAngleX - 0.7F) * 0.75F;
            b.bipedRightArm.rotateAngleX = (float)((double)b.bipedRightArm.rotateAngleX - ((double)f7 * 1.2D + (double)f8));
            b.bipedRightArm.rotateAngleY += b.bipedBody.rotateAngleY * 2.0F;
            b.bipedRightArm.rotateAngleZ = MathHelper.sin(b.onGround * (float)Math.PI) * -0.4F;
        }

        if (p_78087_7_ instanceof EntityPlayer) {
            if (onGround2 > -9990.0F) {
	        	f6 = onGround2;
	            b.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * (float)Math.PI * 2.0F) * 0.2F;
	            b.bipedRightArm.rotationPointZ = MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
	            b.bipedRightArm.rotationPointX = -MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
	            b.bipedLeftArm.rotationPointZ = -MathHelper.sin(b.bipedBody.rotateAngleY) * 5.0F;
	            b.bipedLeftArm.rotationPointX = MathHelper.cos(b.bipedBody.rotateAngleY) * 5.0F;
	            b.bipedRightArm.rotateAngleY += b.bipedBody.rotateAngleY;
	            b.bipedLeftArm.rotateAngleY += b.bipedBody.rotateAngleY;
	            b.bipedLeftArm.rotateAngleX += b.bipedBody.rotateAngleY;
	            f6 = 1.0F - onGround2;
	            f6 *= f6;
	            f6 *= f6;
	            f6 = 1.0F - f6;
	            f7 = MathHelper.sin(f6 * (float)Math.PI);
	            float f8 = MathHelper.sin(onGround2 * (float)Math.PI) * -(b.bipedHead.rotateAngleX - 0.7F) * 0.75F;
	            b.bipedLeftArm.rotateAngleX = (float)((double)b.bipedLeftArm.rotateAngleX - ((double)f7 * 1.2D + (double)f8));
	            b.bipedLeftArm.rotateAngleY -= b.bipedBody.rotateAngleY * 2.0F;
				b.bipedLeftArm.rotateAngleZ = -MathHelper.sin(onGround2  * (float)Math.PI) * -0.4F;
            }
        }

        if (b.isSneak)
        {
            b.bipedBody.rotateAngleX = 0.5F;
            b.bipedRightArm.rotateAngleX += 0.4F;
            b.bipedLeftArm.rotateAngleX += 0.4F;
            b.bipedRightLeg.rotationPointZ = 4.0F;
            b.bipedLeftLeg.rotationPointZ = 4.0F;
            b.bipedRightLeg.rotationPointY = 9.0F;
            b.bipedLeftLeg.rotationPointY = 9.0F;
            b.bipedHead.rotationPointY = 1.0F;
            b.bipedHeadwear.rotationPointY = 1.0F;
        }
        else
        {
            b.bipedBody.rotateAngleX = 0.0F;
            b.bipedRightLeg.rotationPointZ = 0.1F;
            b.bipedLeftLeg.rotationPointZ = 0.1F;
            b.bipedRightLeg.rotationPointY = 12.0F;
            b.bipedLeftLeg.rotationPointY = 12.0F;
            b.bipedHead.rotationPointY = 0.0F;
            b.bipedHeadwear.rotationPointY = 0.0F;
        }

        b.bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        b.bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        b.bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        b.bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;

        if (b.aimedBow)
        {
            f6 = 0.0F;
            f7 = 0.0F;
            b.bipedRightArm.rotateAngleZ = 0.0F;
            b.bipedLeftArm.rotateAngleZ = 0.0F;
            b.bipedRightArm.rotateAngleY = -(0.1F - f6 * 0.6F) + b.bipedHead.rotateAngleY;
            b.bipedLeftArm.rotateAngleY = 0.1F - f6 * 0.6F + b.bipedHead.rotateAngleY + 0.4F;
            b.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + b.bipedHead.rotateAngleX;
            b.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + b.bipedHead.rotateAngleX;
            b.bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
            b.bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
            b.bipedRightArm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
            b.bipedLeftArm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
            b.bipedRightArm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
            b.bipedLeftArm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        }
    }

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	public static boolean processPlayerDigging(NetHandlerPlayServer serv, C07PacketPlayerDigging p_147345_1_)
    {
		WorldServer worldserver = MinecraftServer.getServer().worldServerForDimension(serv.playerEntity.dimension);
	    serv.playerEntity.func_143004_u();

	    if (p_147345_1_.func_149506_g() == 4)
	    {
	        serv.playerEntity.dropOneItem(false);
	        return true;
	    }
	    else if (p_147345_1_.func_149506_g() == 3)
	    {
	        serv.playerEntity.dropOneItem(true);
	        return true;
	    }
	    else if (p_147345_1_.func_149506_g() == 5)
	    {
	        serv.playerEntity.stopUsingItem();
	        return true;
	    }
	    else
	    {
	        boolean flag = false;

	        if (p_147345_1_.func_149506_g() == 0)
	        {
	            flag = true;
	        }

	        if (p_147345_1_.func_149506_g() == 1)
	        {
	            flag = true;
	        }

	        if (p_147345_1_.func_149506_g() == 2)
	        {
	            flag = true;
	        }

	        int i = p_147345_1_.func_149505_c();
	        int j = p_147345_1_.func_149503_d();
	        int k = p_147345_1_.func_149502_e();
	        if (flag)
	        {
	            double d0 = serv.playerEntity.posX - ((double)i + 0.5D);
	            double d1 = serv.playerEntity.posY - ((double)j + 0.5D) + 1.5D;
	            double d2 = serv.playerEntity.posZ - ((double)k + 0.5D);
	            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

	            double dist = serv.playerEntity.theItemInWorldManager.getBlockReachDistance() + 1;
	            dist *= dist;

	            if (d3 > dist)
	            {
	                return true;
	            }
	        }

	        if (p_147345_1_.func_149506_g() == 2)
	        {
	        	customUncheckedTryHarvestBlock(serv.playerEntity.theItemInWorldManager, i, j, k);
	            serv.playerEntity.theItemInWorldManager.uncheckedTryHarvestBlock(i, j, k);

	            if (worldserver.getBlock(i, j, k).getMaterial() != Material.air)
	            {
	                serv.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
	            }
	            return true;
	        }
	        else if (p_147345_1_.func_149506_g() == 1)
	        {
	            serv.playerEntity.theItemInWorldManager.cancelDestroyingBlock(i, j, k);

	            if (worldserver.getBlock(i, j, k).getMaterial() != Material.air)
	            {
	                serv.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
	            }
	            return true;
	        }
	    }
	    return false;
    }

	// This might be a bad idea. (but if I didn't do this I would have to insert ~10 more fixes into forge-hooked methods and it might not even have worked)
	@Fix
	public static void uncheckedTryHarvestBlock(ItemInWorldManager m, int p_73082_1_, int p_73082_2_, int p_73082_3_)
    {
        m.theWorld.destroyBlockInWorldPartially(m.thisPlayerMP.getEntityId(), p_73082_1_, p_73082_2_, p_73082_3_, -1);
        m.tryHarvestBlock(p_73082_1_, p_73082_2_, p_73082_3_);
    }

	public static void customUncheckedTryHarvestBlock(ItemInWorldManager m, int p_73082_1_, int p_73082_2_, int p_73082_3_)
    {
        m.theWorld.destroyBlockInWorldPartially(m.thisPlayerMP.getEntityId(), p_73082_1_, p_73082_2_, p_73082_3_, -1);
        m.tryHarvestBlock(p_73082_1_, p_73082_2_, p_73082_3_);
    }

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	public static boolean onPlayerStoppedUsing(ItemBow bow, ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_)
    {
		if (!(BattlegearUtils.getOffhandItem(p_77615_3_) != null && p_77615_3_.inventory.getCurrentItem() != null && BattlegearUtils.getOffhandItem(p_77615_3_).getItem() == Items.bow && p_77615_3_.inventory.getCurrentItem().getItem() == Items.bow)) {
        	return false;
        }

        int j = bow.getMaxItemUseDuration(p_77615_1_) - p_77615_4_;

        ArrowLooseEvent event = new ArrowLooseEvent(p_77615_3_, p_77615_1_, j);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
        {
            return true;
        }
        j = event.charge;

        boolean flag = p_77615_3_.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, p_77615_1_) > 0;

        if (flag || p_77615_3_.inventory.hasItem(Items.arrow))
        {
            float f = j / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;

            if (f < 0.1D)
            {
                return true;
            }

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            EntityArrow entityarrow = new EntityArrow(p_77615_2_, p_77615_3_, f * 2.0F);

            if (flag)
            {
                entityarrow.canBePickedUp = 2;
            }
            else
            {
                p_77615_3_.inventory.consumeInventoryItem(Items.arrow);
            }

            boolean hasEnough = p_77615_3_.inventory.hasItem(Items.arrow) || flag;

            if (!hasEnough) {
            	p_77615_3_.inventory.addItemStackToInventory(new ItemStack(Items.arrow, 1));
            	return true;
            } else {
            	if (!flag) p_77615_3_.inventory.consumeInventoryItem(Items.arrow);
            }

            if (f == 1.0F)
            {
                entityarrow.setIsCritical(true);
            }

            int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, p_77615_1_);

            if (k > 0)
            {
                entityarrow.setDamage(entityarrow.getDamage() + k * 0.5D + 0.5D);
            }

            int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, p_77615_1_);

            if (l > 0)
            {
                entityarrow.setKnockbackStrength(l);
            }

            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, p_77615_1_) > 0)
            {
                entityarrow.setFire(100);
            }

            p_77615_1_.damageItem(1, p_77615_3_);
            p_77615_2_.playSoundAtEntity(p_77615_3_, "random.bow", 1.0F, 1.0F / (p_77615_2_.rand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

            if (!p_77615_2_.isRemote)
            {
                p_77615_2_.spawnEntityInWorld(entityarrow);

            }
        }

        return BattlegearUtils.getOffhandItem(p_77615_3_) != null && p_77615_3_.inventory.getCurrentItem() != null && BattlegearUtils.getOffhandItem(p_77615_3_).getItem() instanceof ItemBow && p_77615_3_.inventory.getCurrentItem().getItem() instanceof ItemBow;
    }

    @Fix(returnSetting=EnumReturnSetting.ALWAYS)
    public static void processHeldItemChange(NetHandlerPlayServer server, C09PacketHeldItemChange p_147355_1_)
    {
        if (p_147355_1_.func_149614_c() >= 0 && p_147355_1_.func_149614_c() < (InventoryPlayer.getHotbarSize()) || p_147355_1_.func_149614_c() == InventoryPlayerBattle.OFFHAND_HOTBAR_SLOT)
        {
            server.playerEntity.inventory.currentItem = p_147355_1_.func_149614_c();
            server.playerEntity.func_143004_u();
        }
        else
        {
            System.out.println(server.playerEntity.getCommandSenderName() + " tried to set an invalid carried item " + p_147355_1_.func_149614_c());
        }
    }

    @Fix(insertOnExit=true,returnSetting=EnumReturnSetting.ON_NOT_NULL)
    public static ItemStack getCurrentItem(InventoryPlayer inv)
    {
        return inv.currentItem < 9 && inv.currentItem >= 0 ? inv.mainInventory[inv.currentItem] : inv.currentItem == InventoryPlayerBattle.OFFHAND_HOTBAR_SLOT ? BattlegearUtils.getOffhandItem(inv.player) : null;
    }

    @SideOnly(Side.CLIENT)
    @Fix(returnSetting=EnumReturnSetting.NEVER)
    public static void func_147112_ai(Minecraft mc)
    {
        if (mc.objectMouseOver != null)
        {
            boolean flag = mc.thePlayer.capabilities.isCreativeMode && mc.thePlayer.inventoryContainer instanceof ContainerPlayerBattle;
            int j;

            if (!net.minecraftforge.common.ForgeHooks.onPickBlock(mc.objectMouseOver, mc.thePlayer, mc.theWorld)) return;
            // We delete this code wholly instead of commenting it out, to make sure we detect changes in it between MC versions
            if (flag)
            {
                j = mc.thePlayer.inventoryContainer.inventorySlots.size() - 10 + mc.thePlayer.inventory.currentItem;
                mc.playerController.sendSlotPacket(mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem), j);
                mc.objectMouseOver = null;
            }
        }
    }

    private static final MethodHandle fieldGetSection;
    private static final MethodHandle fieldGetContainerMgr;

//    private static final MethodHandle fieldSetLightLevel;
//    private static final Field fieldlightlevel;
//
//    private static final MethodHandle fieldGetEntity;
//    private static final Field fieldentity;
//
//    private static final MethodHandle fieldGetEnabled;
//    private static final Field fieldenabled;

    static {
        MethodHandle fs, fs2, fg, fg2, fg3;
        Field f, f2, f3, f4;
        try {
            //f = HandlerCheckPin.class.getDeclaredField("shouldReset");
            //f.setAccessible(true);
            //fs = MethodHandles.publicLookup().unreflectSetter(f);

            f2 = InvTweaksContainerSectionManager.class.getDeclaredField("containerMgr");
            f3 = InvTweaksContainerSectionManager.class.getDeclaredField("section");

            f2.setAccessible(true);
            f3.setAccessible(true);

            fg = MethodHandles.publicLookup().unreflectGetter(f2);
            fg2 = MethodHandles.publicLookup().unreflectGetter(f3);
        } catch (Exception e) {
            f = null;
            fs = null;
            fg = null;
            fg2 = null;
            System.out.println("The 'Locks' mod compatibility hasn't been loaded due to not being able to find HandlerCheckPin. " +
                    "If you don't have the Locks mod installed, you can ignore this error.");
        } catch (NoClassDefFoundError e) {
            f = null;
            fs = null;
            fg = null;
            fg2 = null;
            System.out.println("The 'Locks' mod compatibility hasn't been loaded due to not being able to find HandlerCheckPin. " +
                    "If you don't have the Locks mod installed, you can ignore this error.");
        }

//        try {
//            f2 = PlayerSelfAdaptor.class.getDeclaredField("thePlayer");
//            f3 = BaseAdaptor.class.getDeclaredField("lightLevel");
//            f4 = BaseAdaptor.class.getDeclaredField("enabled");
//
//            f2.setAccessible(true);
//            f3.setAccessible(true);
//            f4.setAccessible(true);
//
//            fs2 = MethodHandles.publicLookup().unreflectSetter(f3);
//            fg = MethodHandles.publicLookup().unreflectGetter(f2);
//            fg2 = MethodHandles.publicLookup().unreflectGetter(f4);
//        } catch (Exception e) {
//            f2 = f3 = f4 = null;
//            fs2 = fg = fg2 = fg3 = null;
//            System.out.println("The 'Dynamic Lights' mod compatibility hasn't been loaded due to not being able to find BaseAdaptor. " +
//                    "If you don't have the Dynamic Lights mod installed, you can ignore this error.");
//        } catch (NoClassDefFoundError e) {
//            f2 = f3 = f4 = null;
//            fs2 = fg = fg2 = fg3 = null;
//            System.out.println("The 'Dynamic Lights' mod compatibility hasn't been loaded due to not being able to find BaseAdaptor. " +
//                    "If you don't have the Dynamic Lights mod installed, you can ignore this error.");
//        }

        //field = f;
        //fieldSet = fs;

        fieldGetContainerMgr = fg;
        fieldGetSection = fg2;

//        fieldlightlevel = f3;
//        fieldentity = f2;
//        fieldenabled = f4;
//
//        fieldGetEnabled = fg2;
//        fieldGetEntity = fg;
//        fieldSetLightLevel = fs2;

        System.out.println("Loaded Mod Compatibility!");
    }

    @Optional.Method(modid="inventorytweaks")
    public static ContainerSection getContainerSection(InvTweaksContainerSectionManager itcm) {
        ContainerSection section;
        try {
            section = (ContainerSection) fieldGetSection.invokeExact((InvTweaksContainerSectionManager)itcm);
        } catch (Throwable e) {
            /*System.out.println("The 'Inventory Tweaks' mod compatibility hasn't been loaded due to not being able to find ContainerSection. " +
                    "If you don't have the 'Inventory Tweaks' mod installed, you can ignore this error.");*/
            section = null;
        }
        return section;
    }

    @Optional.Method(modid="inventorytweaks")
    public static InvTweaksContainerManager getContainerManager(InvTweaksContainerSectionManager itcm) {
        InvTweaksContainerManager manager;
        try {
            manager = (InvTweaksContainerManager) fieldGetContainerMgr.invokeExact((InvTweaksContainerSectionManager)itcm);
        } catch (Throwable e) {
            /*System.out.println("The 'Inventory Tweaks' mod compatibility hasn't been loaded due to not being able to find InvTweaksContainerManager. " +
                    "If you don't have the 'Inventory Tweaks' mod installed, you can ignore this error.");*/
            manager = null;
        }
        return manager;
    }

    // inv tweaks compat starts here

    /*@Optional.Method(modid="inventorytweaks")
    @Fix(returnSetting = EnumReturnSetting.ALWAYS)
    public static boolean move(InvTweaksContainerSectionManager itcm, int srcIndex, int destIndex) {
        if (CommonProxy.invTweaksDisableMove > 0) {
            CommonProxy.invTweaksDisableMove--;
            return getContainerManager(itcm).move(getContainerSection(itcm), srcIndex, getContainerSection(itcm), srcIndex);
        }

        return getContainerManager(itcm).move(getContainerSection(itcm), srcIndex, getContainerSection(itcm), destIndex);
    }*/
}
