package jagm.classicpipes.client.screen;

import jagm.classicpipes.inventory.DiamondPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DiamondPipeScreen extends AbstractContainerScreen<DiamondPipeMenu> {

    public DiamondPipeScreen(DiamondPipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }

}
