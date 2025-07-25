package jagm.classicpipes.client.screen;

import jagm.classicpipes.inventory.menu.RequestMenu;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RequestScreen extends AbstractContainerScreen<RequestMenu> {

    private static final ResourceLocation BACKGROUND = MiscUtil.resourceLocation("textures/gui/container/request.png");

    private String search;
    private int page;

    public RequestScreen(RequestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.search = "";
        this.page = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float f) {
        super.render(graphics, x, y, f);
        this.renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float f, int x, int y) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

}
