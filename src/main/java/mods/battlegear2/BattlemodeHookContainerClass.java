package mods.battlegear2;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.packet.BattlegearSyncItemPacket;
import mods.battlegear2.packet.OffhandPlaceBlockPacket;
import mods.battlegear2.utils.EnumBGAnimations;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;
import xonin.backhand.Backhand;
import xonin.backhand.client.ClientEventHandler;
import xonin.backhand.client.ClientTickHandler;

public final class BattlemodeHookContainerClass {

    public static final BattlemodeHookContainerClass INSTANCE = new BattlemodeHookContainerClass();

    private BattlemodeHookContainerClass(){}

    private boolean isFake(Entity entity){
        return entity instanceof FakePlayer;
    }
    /**
     * Crash the game if our inventory has been replaced by something else, or the coremod failed
     * Also synchronize battle inventory
     * @param event that spawned the player
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoin(EntityJoinWorldEvent event){
        if (event.entity instanceof EntityPlayer && !(isFake(event.entity))) {
            if (!(((EntityPlayer) event.entity).inventory instanceof InventoryPlayerBattle)) {
                throw new RuntimeException("Player inventory has been replaced with " + ((EntityPlayer) event.entity).inventory.getClass());
            }
            if(event.entity instanceof EntityPlayerMP){
            	Backhand.packetHandler.sendPacketToPlayer(
                        new BattlegearSyncItemPacket((EntityPlayer) event.entity).generatePacket(),
                        (EntityPlayerMP) event.entity);

            }
        }
    }
    
    public static MovingObjectPosition getRaytraceBlock(EntityPlayer p) {
    	float scaleFactor = 1.0F;
		float rotPitch = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * scaleFactor;
		float rotYaw = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * scaleFactor;
		double testX = p.prevPosX + (p.posX - p.prevPosX) * scaleFactor;
		double testY = p.prevPosY + (p.posY - p.prevPosY) * scaleFactor + 1.62D - p.yOffset;//1.62 is player eye height
		double testZ = p.prevPosZ + (p.posZ - p.prevPosZ) * scaleFactor;
		Vec3 testVector = Vec3.createVectorHelper(testX, testY, testZ);
		float var14 = MathHelper.cos(-rotYaw * 0.017453292F - (float)Math.PI);
		float var15 = MathHelper.sin(-rotYaw * 0.017453292F - (float)Math.PI);
		float var16 = -MathHelper.cos(-rotPitch * 0.017453292F);
		float vectorY = MathHelper.sin(-rotPitch * 0.017453292F);
		float vectorX = var15 * var16;
		float vectorZ = var14 * var16;
		double reachLength = 5.0D;
		Vec3 testVectorFar = testVector.addVector(vectorX * reachLength, vectorY * reachLength, vectorZ * reachLength);
		return p.worldObj.rayTraceBlocks(testVector, testVectorFar, false);
    }
    
    public static List<IInventory> tobeclosed = new ArrayList<IInventory>();

    @SubscribeEvent
    public void playerInteract(PlayerInteractEvent event) {
        if(isFake(event.entityPlayer))
            return;

        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {//Right click
            ItemStack mainHandItem = event.entityPlayer.getCurrentEquippedItem();
            ItemStack offhandItem = ((InventoryPlayerBattle) event.entityPlayer.inventory).getOffhandItem();

            if (mainHandItem != null && (BattlegearUtils.checkForRightClickFunction(mainHandItem) || offhandItem == null)) {
                return;
            }

            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && mainHandItem != null && mainHandItem.getItem() instanceof ItemMonsterPlacer) {
                if (event.world.isRemote && !event.entityPlayer.capabilities.isCreativeMode) {
                    mainHandItem.stackSize--;
                }
            }
            //if (!MysteriumPatchesFixesO.shouldNotOverride) {
                PlayerInteractEvent.Result blk = event.useBlock;
                PlayerInteractEvent.Result itm = event.useItem;
                event.useBlock = PlayerInteractEvent.Result.DENY;
                MovingObjectPosition mop = getRaytraceBlock(event.entityPlayer);
                if (Backhand.proxy.isRightClickHeld() && !MysteriumPatchesFixesO.leftclicked) {
                    event.setCanceled(true);
                }
                if (mop != null) {
                    event.setCanceled(true);
                    int
                    i = mop.blockX,
                    j = mop.blockY,
                    k = mop.blockZ,
                    side = mop.sideHit;
                    float f = (float)mop.hitVec.xCoord - i;
                    float f1 = (float)mop.hitVec.yCoord - j;
                    float f2 = (float)mop.hitVec.zCoord - k;

                    if (!event.entityPlayer.isSneaking() && ClientTickHandler.canBlockBeInteractedWith(event.entityPlayer.worldObj, i, j, k)) {
                        event.setCanceled(false);
                        event.useBlock = blk;
                        event.useItem = itm;
                    }
                }
                if (event.entityPlayer.worldObj.isRemote && !BattlegearUtils.usagePriorAttack(offhandItem)) {
                    sendOffSwingEventNoCheck(mainHandItem, offhandItem);
                }
            //}
        }
    }

    public static boolean changedHeldItemTooltips = false;
    // used in hostwapping the item to dig with, to remember where to return the main slot to
    public static int prevOffhandOffset;

    public static boolean isItemBlock(Item item) {
    	return item instanceof ItemBlock || item instanceof ItemDoor || item instanceof ItemSign || item instanceof ItemReed || item instanceof ItemSeedFood || item instanceof ItemRedstone || item instanceof ItemBucket || item instanceof ItemSkull;
    }
    
    @SideOnly(Side.CLIENT)
    public static void tryBreakBlockOffhand(MovingObjectPosition objectMouseOver, ItemStack offhandItem, ItemStack mainHandItem, PlayerTickEvent event) {
    	Minecraft mcInstance = Minecraft.getMinecraft();
        int i = objectMouseOver.blockX;
        int j = objectMouseOver.blockY;
        int k = objectMouseOver.blockZ;
        if (mcInstance.thePlayer.capabilities.isCreativeMode) 
        {
        	if (ClientEventHandler.delay <= 0) {
            	mcInstance.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(0, i, j, k, objectMouseOver.sideHit));
            	PlayerControllerMP.clickBlockCreative(mcInstance, mcInstance.playerController, i, j, k, objectMouseOver.sideHit);
            	if (!(event.player.worldObj.isRemote && !(BattlegearUtils.usagePriorAttack(offhandItem)))) {
            		sendOffSwingEventNoCheck(mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
                }
            	ClientEventHandler.delay = 24;
        	}
        	return;
        }
        if (mcInstance.theWorld.getBlock(i, j, k).getMaterial() != Material.air)
        {
        	if (mcInstance.playerController.blockHitDelay > 0)
            {
                --mcInstance.playerController.blockHitDelay;
            }
            else
            {
            	mcInstance.playerController.isHittingBlock = true;
                mcInstance.playerController.currentBlockX = i;
                mcInstance.playerController.currentBlockY = j;
                mcInstance.playerController.currentblockZ = k;

            	if ((!ItemStack.areItemStacksEqual(((InventoryPlayerBattle)mcInstance.thePlayer.inventory).getOffhandItem(), mcInstance.thePlayer.inventory.getStackInSlot(mcInstance.thePlayer.inventory.currentItem))) && (((InventoryPlayerBattle)mcInstance.thePlayer.inventory).getOffhandItem() != null))
            	{
            		if (mcInstance.gameSettings.heldItemTooltips) {
            			mcInstance.gameSettings.heldItemTooltips = false;
            			changedHeldItemTooltips = true;   			
            		}
            		
            		//prevOffhandOffset = ((InventoryPlayerBattle)mcInstance.thePlayer.inventory).getOffsetToInactiveHand();
                    //mcInstance.thePlayer.inventory.currentItem += ((InventoryPlayerBattle)mcInstance.thePlayer.inventory).getOffsetToInactiveHand();
            		mcInstance.playerController.currentItemHittingBlock = ((InventoryPlayerBattle)mcInstance.thePlayer.inventory).getOffhandItem();
                    mcInstance.playerController.syncCurrentPlayItem();
            	}

                
                Block block = mcInstance.theWorld.getBlock(i, j, k);
                if (block.getMaterial() == Material.air)
                {
                    mcInstance.playerController.isHittingBlock = false;
                    return;
                }
                MysteriumPatchesFixesO.countToCancel = 5;
                mcInstance.playerController.curBlockDamageMP += block.getPlayerRelativeBlockHardness(mcInstance.thePlayer, mcInstance.thePlayer.worldObj, i, j, k);

                if (mcInstance.playerController.stepSoundTickCounter % 4.0F == 0.0F)
                {
                	mcInstance.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepResourcePath()), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getPitch() * 0.5F, (float)i + 0.5F, (float)j + 0.5F, (float)k + 0.5F));
                }

                ++mcInstance.playerController.stepSoundTickCounter;

                if (mcInstance.playerController.curBlockDamageMP >= 1.0F)
                {
                    
                    ItemStack itemstack = mcInstance.thePlayer.getCurrentEquippedItem();

                    if (itemstack != null)
                    {
                    	itemstack.func_150999_a(mcInstance.theWorld, block, i, j, k, mcInstance.thePlayer);

                        if (itemstack.stackSize == 0)
                        {
                        	mcInstance.thePlayer.destroyCurrentEquippedItem();
                        }
                    }
                    mcInstance.playerController.isHittingBlock = false;
                    mcInstance.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(2, i, j, k, objectMouseOver.sideHit));
                    mcInstance.playerController.onPlayerDestroyBlock(i, j, k, objectMouseOver.sideHit);
                    mcInstance.playerController.curBlockDamageMP = 0.0F;
                    mcInstance.playerController.stepSoundTickCounter = 0.0F;
                    mcInstance.playerController.blockHitDelay = 5;
                }
                mcInstance.theWorld.destroyBlockInWorldPartially(mcInstance.thePlayer.getEntityId(), mcInstance.playerController.currentBlockX, mcInstance.playerController.currentBlockY, mcInstance.playerController.currentblockZ, (int)(mcInstance.playerController.curBlockDamageMP * 10.0F) - 1);
            }

            if (mcInstance.thePlayer.isCurrentToolAdventureModeExempt(i, j, k))
            {
            	mcInstance.effectRenderer.addBlockHitEffects(i, j, k, objectMouseOver);
            }
            if (!(event.player.worldObj.isRemote && !(BattlegearUtils.usagePriorAttack(offhandItem)))) {
            	sendOffSwingEventNoCheck(mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
            }
        }
	}

	/**
     * Attempts to right-click-use an item by the given EntityPlayer
     */
    public static boolean tryUseItem(EntityPlayer entityPlayer, ItemStack itemStack, Side side)
    {
        if(side.isClient()){
        	Backhand.packetHandler.sendPacketToServer(new OffhandPlaceBlockPacket(-1, -1, -1, 255, itemStack, 0.0F, 0.0F, 0.0F).generatePacket());
        }
        final int i = itemStack.stackSize;
        final int j = itemStack.getItemDamage();
        ItemStack itemstack1 = itemStack.useItemRightClick(entityPlayer.getEntityWorld(), entityPlayer);

        if (itemstack1 == itemStack && (itemstack1 == null || itemstack1.stackSize == i && (side.isServer()?(itemstack1.getMaxItemUseDuration() <= 0 && itemstack1.getItemDamage() == j):true)))
        {
            return false;
        }
        else
        {
            BattlegearUtils.setPlayerOffhandItem(entityPlayer, itemstack1);
            if (side.isServer() && (entityPlayer).capabilities.isCreativeMode)
            {
                itemstack1.stackSize = i;
                if (itemstack1.isItemStackDamageable())
                {
                    itemstack1.setItemDamage(j);
                }
            }
            if (itemstack1.stackSize <= 0)
            {
                BattlegearUtils.setPlayerOffhandItem(entityPlayer, null);
                ForgeEventFactory.onPlayerDestroyItem(entityPlayer, itemstack1);
            }
            if (side.isServer() && !entityPlayer.isUsingItem())
            {
                ((EntityPlayerMP)entityPlayer).sendContainerToPlayer(entityPlayer.inventoryContainer);
            }
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendOffSwingEvent(PlayerEvent event, ItemStack mainHandItem, ItemStack offhandItem){
        if(!MinecraftForge.EVENT_BUS.post(new PlayerEventChild.OffhandSwingEvent(event, mainHandItem, offhandItem))){
            ((IBattlePlayer) event.entityPlayer).swingOffItem();
            Backhand.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, event.entityPlayer);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendOffSwingEventNoCheck(ItemStack mainHandItem, ItemStack offhandItem){
        ((IBattlePlayer) Minecraft.getMinecraft().thePlayer).swingOffItem();
        Backhand.proxy.sendAnimationPacket(EnumBGAnimations.OffHandSwing, Minecraft.getMinecraft().thePlayer);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onOffhandSwing(PlayerEventChild.OffhandSwingEvent event){
        if(MysteriumPatchesFixesO.shouldNotOverride){
            event.setCanceled(true);
            event.setCancelParentEvent(false);
        }
    }
    
    public boolean interactWithNoEvent(EntityPlayer pl, Entity p_70998_1_)
    {
        ItemStack itemstack = pl.getCurrentEquippedItem();
        ItemStack itemstack1 = itemstack != null ? itemstack.copy() : null;

        if (!p_70998_1_.interactFirst(pl))
        {
            if (itemstack != null && p_70998_1_ instanceof EntityLivingBase)
            {
                if (pl.capabilities.isCreativeMode)
                {
                    itemstack = itemstack1;
                }

                if (itemstack.interactWithEntity(pl, (EntityLivingBase)p_70998_1_))
                {
                    if (itemstack.stackSize <= 0 && !pl.capabilities.isCreativeMode)
                    {
                        pl.destroyCurrentEquippedItem();
                    }

                    return true;
                }
            }

            return false;
        }
        else
        {
            if (itemstack != null && itemstack == pl.getCurrentEquippedItem())
            {
                if (itemstack.stackSize <= 0 && !pl.capabilities.isCreativeMode)
                {
                    pl.destroyCurrentEquippedItem();
                }
                else if (itemstack.stackSize < itemstack1.stackSize && pl.capabilities.isCreativeMode)
                {
                    itemstack.stackSize = itemstack1.stackSize;
                }
            }

            return true;
        }
    }
    
    public static boolean interactWith = false;

    @SubscribeEvent
    public void playerInteractEntity(EntityInteractEvent event) {
        if(isFake(event.entityPlayer))
            return;
        interactWith = interactWithNoEvent(event.entityPlayer, event.target);
        ItemStack offhandItem = ((InventoryPlayerBattle)event.entityPlayer.inventory).getOffhandItem();
        if(!BattlegearUtils.usagePriorAttack(offhandItem)){
            ItemStack mainHandItem = event.entityPlayer.getCurrentEquippedItem();
            PlayerEventChild.OffhandAttackEvent offAttackEvent = new PlayerEventChild.OffhandAttackEvent(event, mainHandItem, offhandItem);
            //if (!MysteriumPatchesFixesO.shouldNotOverride) {
                if(!MinecraftForge.EVENT_BUS.post(offAttackEvent)){
                    if (interactWith) {
                        interactWith = false;
                        return;
                    } else {
                        event.setCanceled(true);
                    }
                    if (offAttackEvent.swingOffhand){
                        if (event.entityPlayer.worldObj.isRemote) sendOffSwingEvent(event, mainHandItem, offhandItem);
                    }
                    if (offAttackEvent.shouldAttack)
                    {
                        ((IBattlePlayer) event.entityPlayer).attackTargetEntityWithCurrentOffItem(event.target);
                    }
                    if (offAttackEvent.cancelParent) {
                        event.setCanceled(true);
                    }
                }
            //}
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onOffhandAttack(PlayerEventChild.OffhandAttackEvent event){
    	if(event.offHand != null){
            if(hasEntityInteraction(event.getPlayer().capabilities.isCreativeMode?event.offHand.copy():event.offHand, event.getTarget(), event.getPlayer(), false)){
            	event.setCanceled(true);
                if(event.offHand.stackSize<=0 && !event.getPlayer().capabilities.isCreativeMode){
                    ItemStack orig = event.offHand;
                    BattlegearUtils.setPlayerOffhandItem(event.getPlayer(), null);
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.getPlayer(), orig));
                }
            }
        }
    }

    /**
     * Check if a stack has a specific interaction with an entity.
     * Use a call to {@link net.minecraft.item.ItemStack#interactWithEntity(EntityPlayer, EntityLivingBase)}
     *
     * @param itemStack to interact last with
     * @param entity to interact first with
     * @param entityPlayer holding the stack
     * @param asTest if data should be cloned before testing
     * @return true if a specific interaction exist (and has been done if asTest is false)
     */
    private boolean hasEntityInteraction(ItemStack itemStack, Entity entity, EntityPlayer entityPlayer, boolean asTest){
        if (asTest) {
            Entity clone = EntityList.createEntityByName(EntityList.getEntityString(entity), entity.worldObj);
            if (clone != null) {
                clone.copyDataFrom(entity, true);
                return !clone.interactFirst(entityPlayer) && clone instanceof EntityLivingBase && itemStack.copy().interactWithEntity(entityPlayer, (EntityLivingBase) clone);
            }
        } else if(!entity.interactFirst(entityPlayer) && entity instanceof EntityLivingBase){
            return itemStack.interactWithEntity(entityPlayer, (EntityLivingBase) entity);
        }
        return false;
    }

    @SubscribeEvent
    public void addTracking(PlayerEvent.StartTracking event){
        if(event.target instanceof EntityPlayer && !isFake(event.target)){
            ((EntityPlayerMP)event.entityPlayer).playerNetServerHandler.sendPacket(new BattlegearSyncItemPacket((EntityPlayer) event.target).generatePacket());
        }
    }
}
