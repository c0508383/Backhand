package mods.battlegear2.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.ResourceLocation;
import xonin.backhand.Backhand;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.packet.OffhandPlaceBlockPacket;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;
import xonin.backhand.client.ClientEventHandler;
import xonin.backhand.client.ClientTickHandler;

public final class BattlegearClientTickHandler {
	public static final int FLASH_MAX = 30;
    // TODO: Add special action to some items, maybe?
    //public final KeyBinding special;
    public final Minecraft mc = Minecraft.getMinecraft();

    public float blockBar = 1;
    public float partialTick;
    public boolean wasBlocking = false;
    public int previousBattlemode = 0;
    public int flashTimer;
    public boolean specialDone = false, drawDone = false, inBattle = false;
    public static final BattlegearClientTickHandler INSTANCE = new BattlegearClientTickHandler();

    public BattlegearClientTickHandler() {
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.player == mc.thePlayer) {
        	if (!MysteriumPatchesFixesO.hotSwapped && ((InventoryPlayerBattle)event.player.inventory).currentItem > 153) {
        		//event.player.inventory.currentItem = previousBattlemode;
                mc.playerController.syncCurrentPlayItem();
        	}
            if (event.phase == TickEvent.Phase.START) {
                tickStart(mc.thePlayer);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void tickStart(EntityPlayer player) {
        ItemStack offhand = ((InventoryPlayerBattle) player.inventory).getOffhandItem();
        if(offhand != null){
            if(mc.gameSettings.keyBindUseItem.getIsKeyPressed() && mc.rightClickDelayTimer == 4 && !player.isUsingItem()){
                tryCheckUseItem(offhand, player);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void tryCheckUseItem(ItemStack offhandItem, EntityPlayer player){
        ItemStack mainHandItem = player.getCurrentEquippedItem();
        if (mainHandItem != null && BattlegearUtils.checkForRightClickFunction(mainHandItem)) {
            return;
        }

    	MovingObjectPosition mop = BattlemodeHookContainerClass.getRaytraceBlock(player);
		if (mop != null && BattlemodeHookContainerClass.canBlockBeInteractedWith(player.worldObj, mop.blockX, mop.blockY, mop.blockZ)) {
			return;
		}
        if (BattlegearUtils.usagePriorAttack(offhandItem)) {
            MovingObjectPosition mouseOver = mc.objectMouseOver;
            boolean flag = true;
            if (mouseOver != null)
            {
                if (mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
                {
                    if(mc.playerController.interactWithEntitySendPacket(player, mouseOver.entityHit))
                        flag = false;
                }
                else if (mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    int j = mouseOver.blockX;
                    int k = mouseOver.blockY;
                    int l = mouseOver.blockZ;
                    if (!player.worldObj.getBlock(j, k, l).isAir(player.worldObj, j, k, l)) {
                        final int size = offhandItem.stackSize;
                        int i1 = mouseOver.sideHit;
                        PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, j, k, l, i1, player.worldObj), offhandItem);
                        if (player.capabilities.allowEdit || !BattlemodeHookContainerClass.isItemBlock(offhandItem.getItem())) {
                            if (!MinecraftForge.EVENT_BUS.post(useItemEvent) && onPlayerPlaceBlock(mc.playerController, player, offhandItem, j, k, l, i1, mouseOver.hitVec)) {
                                ((IBattlePlayer) player).swingOffItem();
                                flag = false;
                            }
                        }
                        if (offhandItem == null)
                        {
                            return;
                        }
                        if (offhandItem.stackSize == 0)
                        {
                            BattlegearUtils.setPlayerOffhandItem(player, null);
                        }
                        else if (offhandItem.stackSize != size || mc.playerController.isInCreativeMode())
                        {
                            //ClientEventHandler.renderOffhandPlayer.itemRenderer.resetEquippedProgress();
                        }
                    }
                }
            }
            if (flag)
            {
                offhandItem = ((InventoryPlayerBattle) player.inventory).getOffhandItem();
                PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, player.worldObj), offhandItem);
                if (offhandItem != null && !MinecraftForge.EVENT_BUS.post(useItemEvent) && BattlemodeHookContainerClass.tryUseItem(player, offhandItem, Side.CLIENT))
                {
                    //ClientEventHandler.renderOffhandPlayer.itemRenderer.resetEquippedProgress();
                }
            }
        }
    }

    private boolean onPlayerPlaceBlock(PlayerControllerMP controller, EntityPlayer player, ItemStack offhand, int i, int j, int k, int l, Vec3 hitVec) {
        float f = (float)hitVec.xCoord - i;
        float f1 = (float)hitVec.yCoord - j;
        float f2 = (float)hitVec.zCoord - k;
        boolean flag = false;
        int i1;
        final World worldObj = player.worldObj;
        if (offhand.getItem().onItemUseFirst(offhand, player, worldObj, i, j, k, l, f, f1, f2)){
            return true;
        }
        if (!player.isSneaking() || ((InventoryPlayerBattle) player.inventory).getOffhandItem() == null || ((InventoryPlayerBattle) player.inventory).getOffhandItem().getItem().doesSneakBypassUse(worldObj, i, j, k, player)){
            Block b = worldObj.getBlock(i, j, k);
            if (!b.isAir(worldObj, i, j, k) && b.onBlockActivated(worldObj, i, j, k, player, l, f, f1, f2)){
                flag = true;
            }
        }
        if (!flag && offhand.getItem() instanceof ItemBlock){
            ItemBlock itemblock = (ItemBlock)offhand.getItem();
            if (!itemblock.func_150936_a(worldObj, i, j, k, l, player, offhand)){
                return false;
            }
        }
        Backhand.packetHandler.sendPacketToServer(new OffhandPlaceBlockPacket(i, j, k, l, offhand, f, f1, f2).generatePacket());
        if (flag){
            return true;
        }
        else if (offhand == null){
            return false;
        }
        else{
            if (controller.isInCreativeMode()){
                i1 = offhand.getItemDamage();
                int j1 = offhand.stackSize;
                boolean flag1 = offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2);
                offhand.setItemDamage(i1);
                offhand.stackSize = j1;
                return flag1;
            }
            else{
                if (!offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2)){
                    return false;
                }
                if (offhand.stackSize <= 0){
                    ForgeEventFactory.onPlayerDestroyItem(player, offhand);
                }
                return true;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void tryBreakBlockOffhand(MovingObjectPosition objectMouseOver, ItemStack offhandItem, ItemStack mainHandItem, TickEvent.PlayerTickEvent event) {
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
                    BattlemodeHookContainerClass.sendOffSwingEventNoCheck(event.player, mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
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
                        BattlemodeHookContainerClass.changedHeldItemTooltips = true;
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
                BattlemodeHookContainerClass.sendOffSwingEventNoCheck(event.player, mainHandItem, offhandItem); // force offhand swing anyway because we broke a block
            }
        }
    }

    public static float getPartialTick(){
        return INSTANCE.partialTick;
    }

    public static ItemStack getPreviousMainhand(EntityPlayer player){
        return player.inventory.getStackInSlot(INSTANCE.previousBattlemode);
    }

    /*public static ItemStack getPreviousOffhand(EntityPlayer player){
        return player.inventory.getStackInSlot(INSTANCE.previousBattlemode+((InventoryPlayerBattle)player.inventory).getOffsetToInactiveHand());
    }*/
}
