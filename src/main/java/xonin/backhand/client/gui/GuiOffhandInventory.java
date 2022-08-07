package xonin.backhand.client.gui;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class GuiOffhandInventory extends GuiInventory {
    private float xSizeFloat;
    private float ySizeFloat;

    public GuiOffhandInventory(EntityPlayer p_i1094_1_) {
        super(p_i1094_1_);
    }

    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        this.xSizeFloat = (float)p_73863_1_;
        this.ySizeFloat = (float)p_73863_2_;
    }

    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(field_147001_a);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(87 + guiLeft, 25 + guiTop, 87, 8, 74, 15);
        this.drawTexturedModalRect(87 + guiLeft, 40 + guiTop, 87, 8, 74, 15);
        this.drawTexturedModalRect(87 + guiLeft, 55 + guiTop, 87, 8, 74, 15);
        this.drawTexturedModalRect(97 + guiLeft, 20 + guiTop, 87, 25, 74, 36);
        this.drawTexturedModalRect(79 + guiLeft, 61 + guiTop, 7, 83, 18, 18);
        func_147046_a(k + 51, l + 75, 30, (float)(k + 51) - this.xSizeFloat, (float)(l + 75 - 50) - this.ySizeFloat, this.mc.thePlayer);
    }

    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        this.fontRendererObj.drawString(I18n.format("container.crafting"), 97, 10, 4210752);
    }
}
