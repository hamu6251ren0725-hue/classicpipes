package jagm.classicpipes.client.screen;

import jagm.classicpipes.inventory.menu.AdvancedCopperFluidPipeMenu;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedCopperFluidPipeScreen extends FluidFilterScreen<AdvancedCopperFluidPipeMenu> {

    private static final Identifier BACKGROUND = MiscUtil.identifier("textures/gui/container/networked_pipe.png");

    public AdvancedCopperFluidPipeScreen(AdvancedCopperFluidPipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

}
