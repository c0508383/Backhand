package net.tclproject.mysteriumlib.asm.fixes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.tclproject.mysteriumlib.asm.annotations.EnumReturnSetting;
import net.tclproject.mysteriumlib.asm.annotations.Fix;
import net.tclproject.mysteriumlib.asm.annotations.ReturnedValue;
import xonin.backhand.client.ClientEventHandler;

public class MysteriumPatchesFixesO {
	/**Whether we have just overriden the minecraft method that gets called on right click to substitute the offhand item*/
	public static boolean leftclicked;

    @Fix(returnSetting=EnumReturnSetting.ALWAYS)
	public static boolean isPlayer(EntityPlayer p) {
		return false;
	}

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	@SideOnly(Side.CLIENT)
	public static boolean func_147116_af(Minecraft mc) {
		if (Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem() == null || (mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK && Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem() != null && !(Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem().getItem() instanceof ItemMonsterPlacer)) || !BattlegearUtils.usagePriorAttack(Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem())) {
			return false;
		}
		KeyBinding keyCode = Minecraft.getMinecraft().gameSettings.keyBindUseItem;
		KeyBinding.setKeyBindState(keyCode.getKeyCode(), true);
		KeyBinding.onTick(keyCode.getKeyCode());
		leftclicked = true;
		return true;
	}

    /**Dirty hack to prevent random resetting of block removal (why does this even happen?!) when breaking blocks with the offhand.*/
    public static int countToCancel = 0;
    /**If we have hotswapped the breaking item with the one in offhand and should hotswap it back when called next*/
    public static boolean hotSwapped = false;

    @SideOnly(Side.CLIENT)
    @Fix(returnSetting=EnumReturnSetting.ON_TRUE)
    public static boolean resetBlockRemoving(PlayerControllerMP controller)
    {
        if (countToCancel > 0) {
            countToCancel--;
            return true;
        } else {
            if (MysteriumPatchesFixesO.hotSwapped) {
                Minecraft.getMinecraft().thePlayer.inventory.currentItem -= BattlemodeHookContainerClass.prevOffhandOffset;
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

	/**Whether the offhand can use it's item right now. Prevents the other animation from playing when we're actually using the item not hitting with it*/
	private static boolean noAltHandUse;
	
	/**Whether we're rendering the item in the main hand right now*/
	public static boolean renderingItem2;

	@Fix
	@SideOnly(Side.CLIENT)
	public static void renderEquippedItems(RenderPlayer p, AbstractClientPlayer p_77029_1_, float p_77029_2_)
    {
		if (!MysteriumPatchesFixesO.leftclicked) noAltHandUse = true;
    }

	@Fix(returnSetting=EnumReturnSetting.ALWAYS)
	public static EnumAction getItemUseAction(ItemStack itmst)
    {
		if (noAltHandUse) {
			noAltHandUse = false;
			return EnumAction.none;
		}
		else return itmst.getItem().getItemUseAction(itmst);
    }

	public static float onGround2;
	
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
				b.bipedLeftArm.rotateAngleZ = MathHelper.sin(onGround2  * (float)Math.PI) * -0.4F;
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

	@SideOnly(Side.CLIENT)
	@Fix(insertOnExit = true, returnSetting = EnumReturnSetting.ALWAYS)
    public static IIcon getItemIcon(EntityPlayer p, ItemStack p_70620_1_, int p_70620_2_, @ReturnedValue IIcon returnValue) 
	{
		if (returnValue == Items.bow.getItemIconForUseDuration(2) || returnValue == Items.bow.getItemIconForUseDuration(1) || returnValue == Items.bow.getItemIconForUseDuration(0)) {
			if (p.getItemInUse().getItem() != Items.bow) {
				return p_70620_1_.getItem().getIcon(p_70620_1_, p_70620_2_, p, p.getItemInUse(), p.getItemInUseCount());
			}
		}
		return returnValue;
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

	

	@Fix(returnSetting = EnumReturnSetting.ALWAYS)
	public static void attackTargetEntityWithCurrentItem(EntityPlayer plr, Entity p_71059_1_)
    {
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(plr, p_71059_1_)))
        {
            return;
        }
        ItemStack stack = plr.getCurrentEquippedItem();
        if (stack != null && stack.getItem().onLeftClickEntity(stack, plr, p_71059_1_))
        {
            return;
        }
        if (p_71059_1_.canAttackWithItem())
        {
            if (!p_71059_1_.hitByEntity(plr))
            {
                float f = (float)plr.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;

                if (p_71059_1_ instanceof EntityLivingBase)
                {
                    f1 = EnchantmentHelper.getEnchantmentModifierLiving(plr, (EntityLivingBase)p_71059_1_);
                    i += EnchantmentHelper.getKnockbackModifier(plr, (EntityLivingBase)p_71059_1_);
                }

                if (plr.isSprinting())
                {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F)
                {
                    boolean flag = plr.fallDistance > 0.0F && !plr.onGround && !plr.isOnLadder() && !plr.isInWater() && !plr.isPotionActive(Potion.blindness) && plr.ridingEntity == null && p_71059_1_ instanceof EntityLivingBase;

                    if (flag && f > 0.0F)
                    {
                        f *= 1.5F;
                    }

                    ItemStack offItem = ((InventoryPlayerBattle)plr.inventory).getOffhandItem();

                    if(offItem != null && offItem.getItem() instanceof ItemSword) {
                    	f += ToolMaterial.valueOf(((ItemSword)offItem.getItem()).getToolMaterialName()).getDamageVsEntity() + 3F;
                    } else if (offItem != null && offItem.getItem() instanceof ItemTool) {
                    	f += ToolMaterial.valueOf(((ItemTool)offItem.getItem()).getToolMaterialName()).getDamageVsEntity() + 1F;
                    }

                    f += f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(plr);

                    if (p_71059_1_ instanceof EntityLivingBase && j > 0 && !p_71059_1_.isBurning())
                    {
                        flag1 = true;
                        p_71059_1_.setFire(1);
                    }

                    boolean flag2 = p_71059_1_.attackEntityFrom(DamageSource.causePlayerDamage(plr), f);

                    if (flag2)
                    {
                        if (i > 0)
                        {
                            p_71059_1_.addVelocity(-MathHelper.sin(plr.rotationYaw * (float)Math.PI / 180.0F) * i * 0.5F, 0.1D, MathHelper.cos(plr.rotationYaw * (float)Math.PI / 180.0F) * i * 0.5F);
                            plr.motionX *= 0.6D;
                            plr.motionZ *= 0.6D;
                            plr.setSprinting(false);
                        }

                        if (flag)
                        {
                            plr.onCriticalHit(p_71059_1_);
                        }

                        if (f1 > 0.0F)
                        {
                            plr.onEnchantmentCritical(p_71059_1_);
                        }

                        if (f >= 18.0F)
                        {
                            plr.triggerAchievement(AchievementList.overkill);
                        }

                        plr.setLastAttacker(p_71059_1_);

                        if (p_71059_1_ instanceof EntityLivingBase)
                        {
                            EnchantmentHelper.func_151384_a((EntityLivingBase)p_71059_1_, plr);
                        }

                        EnchantmentHelper.func_151385_b(plr, p_71059_1_);
                        ItemStack itemstack = plr.getCurrentEquippedItem();
                        Object object = p_71059_1_;

                        if (p_71059_1_ instanceof EntityDragonPart)
                        {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)p_71059_1_).entityDragonObj;

                            if (ientitymultipart != null && ientitymultipart instanceof EntityLivingBase)
                            {
                                object = ientitymultipart;
                            }
                        }

                        if (itemstack != null && object instanceof EntityLivingBase)
                        {
                            itemstack.hitEntity((EntityLivingBase)object, plr);

                            if (itemstack.stackSize <= 0)
                            {
                                plr.destroyCurrentEquippedItem();
                            }
                        }

                        if (p_71059_1_ instanceof EntityLivingBase)
                        {
                            plr.addStat(StatList.damageDealtStat, Math.round(f * 10.0F));

                            if (j > 0)
                            {
                                p_71059_1_.setFire(j * 4);
                            }
                        }

                        plr.addExhaustion(0.3F);
                    }
                    else if (flag1)
                    {
                        p_71059_1_.extinguish();
                    }
                }
            }
        }
    }

	@Fix(returnSetting=EnumReturnSetting.ON_TRUE)
	public static boolean onPlayerStoppedUsing(ItemBow bow, ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_)
    {
		if (!(((InventoryPlayerBattle)p_77615_3_.inventory).getOffhandItem() != null && ((InventoryPlayerBattle)p_77615_3_.inventory).getCurrentItem() != null && ((InventoryPlayerBattle)p_77615_3_.inventory).getOffhandItem().getItem() == Items.bow && ((InventoryPlayerBattle)p_77615_3_.inventory).getCurrentItem().getItem() == Items.bow)) {
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
            EntityArrow entityarrow2 = new EntityArrow(p_77615_2_, p_77615_3_, f * 1.5F);

            if (flag)
            {
                entityarrow.canBePickedUp = 2;
                entityarrow2.canBePickedUp = 2;
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
                entityarrow2.setIsCritical(true);
            }

            int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, p_77615_1_);

            if (k > 0)
            {
                entityarrow.setDamage(entityarrow.getDamage() + k * 0.5D + 0.5D);
                entityarrow2.setDamage(entityarrow2.getDamage() + k * 0.5D + 0.5D);
            }

            int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, p_77615_1_);

            if (l > 0)
            {
                entityarrow.setKnockbackStrength(l);
                entityarrow2.setKnockbackStrength(l);
            }

            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, p_77615_1_) > 0)
            {
                entityarrow.setFire(100);
                entityarrow2.setFire(100);
            }

            p_77615_1_.damageItem(1, p_77615_3_);
            p_77615_2_.playSoundAtEntity(p_77615_3_, "random.bow", 1.0F, 1.0F / (p_77615_2_.rand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

            if (!p_77615_2_.isRemote)
            {
                p_77615_2_.spawnEntityInWorld(entityarrow);
                p_77615_2_.spawnEntityInWorld(entityarrow2);

            }
        }

        if (((InventoryPlayerBattle)p_77615_3_.inventory).getOffhandItem() != null && ((InventoryPlayerBattle)p_77615_3_.inventory).getCurrentItem() != null && ((InventoryPlayerBattle)p_77615_3_.inventory).getOffhandItem().getItem() instanceof ItemBow && ((InventoryPlayerBattle)p_77615_3_.inventory).getCurrentItem().getItem() instanceof ItemBow) {
        	return true;
        } else {
        	return false;
        }
    }
}
