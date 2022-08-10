package xonin.backhand.client.gui;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.packet.OffhandToServerPacket;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;

public class GuiOffhandCreativeInventory extends GuiContainerCreative {

    public GuiOffhandCreativeInventory(EntityPlayer p_i1088_1_) {
        super(p_i1088_1_);
    }

    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        super.drawGuiContainerBackgroundLayer(p_146976_1_,p_146976_2_,p_146976_3_);
        if (selectedTabIndex == CreativeTabs.tabInventory.getTabIndex()) {
            this.mc.getTextureManager().bindTexture(field_147001_a);
            this.drawTexturedModalRect(81 + guiLeft, 32 + guiTop, 7, 83, 18, 18);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.mc.thePlayer.sendQueue.addToSendQueue(
                new OffhandToServerPacket(BattlegearUtils.getOffhandItem(this.mc.thePlayer), this.mc.thePlayer).generatePacket()
        );
    }

    public void setCurrentCreativeTab(CreativeTabs p_147050_1_)
    {
        if (p_147050_1_ == null) return;
        int i = selectedTabIndex;
        selectedTabIndex = p_147050_1_.getTabIndex();
        GuiContainerCreative.ContainerCreative containercreative = (GuiContainerCreative.ContainerCreative)this.inventorySlots;
        this.field_147008_s.clear();
        containercreative.itemList.clear();
        p_147050_1_.displayAllReleventItems(containercreative.itemList);

        if (p_147050_1_ == CreativeTabs.tabInventory)
        {
            Container container = this.mc.thePlayer.inventoryContainer;

            if (this.field_147063_B == null)
            {
                this.field_147063_B = containercreative.inventorySlots;
            }

            containercreative.inventorySlots = new ArrayList();
            for (int j = 0; j < container.inventorySlots.size(); ++j)
            {
                GuiContainerCreative.CreativeSlot creativeslot = new GuiContainerCreative.CreativeSlot((Slot)container.inventorySlots.get(j), j);
                containercreative.inventorySlots.add(creativeslot);
                int k;
                int l;
                int i1;

                if (j == container.inventorySlots.size()-1) {
                    creativeslot.xDisplayPosition = 82;
                    creativeslot.yDisplayPosition = 33;
                } else if (j >= 5 && j < 9) {
                    k = j - 5;
                    l = k / 2;
                    i1 = k % 2;
                    creativeslot.xDisplayPosition = 9 + l * 54;
                    creativeslot.yDisplayPosition = 6 + i1 * 27;
                } else if (j >= 0 && j < 5) {
                    creativeslot.yDisplayPosition = -2000;
                    creativeslot.xDisplayPosition = -2000;
                } else if (j < container.inventorySlots.size()) {
                    k = j - 9;
                    l = k % 9;
                    i1 = k / 9;
                    creativeslot.xDisplayPosition = 9 + l * 18;

                    if (j >= 36)
                    {
                        creativeslot.yDisplayPosition = 112;
                    }
                    else
                    {
                        creativeslot.yDisplayPosition = 54 + i1 * 18;
                    }
                }
            }

            this.field_147064_C = new Slot(field_147060_v, 0, 173, 112);
            containercreative.inventorySlots.add(this.field_147064_C);
        }
        else if (i == CreativeTabs.tabInventory.getTabIndex())
        {
            containercreative.inventorySlots = this.field_147063_B;
            this.field_147063_B = null;
        }

        if (this.searchField != null)
        {
            if (p_147050_1_.hasSearchBar())
            {
                this.searchField.setVisible(true);
                this.searchField.setCanLoseFocus(false);
                this.searchField.setFocused(true);
                this.searchField.setText("");
                this.searchField.width = p_147050_1_.getSearchbarWidth();
                this.searchField.xPosition = this.guiLeft + (82 /*default left*/ + 89 /*default width*/) - this.searchField.width;
                this.updateCreativeSearch();
            }
            else
            {
                this.searchField.setVisible(false);
                this.searchField.setCanLoseFocus(true);
                this.searchField.setFocused(false);
            }
        }

        this.currentScroll = 0.0F;
        containercreative.scrollTo(0.0F);
    }
}
