package jagm.classicpipes.client.screen;

import jagm.classicpipes.inventory.menu.RequestMenu;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RequestScreen extends AbstractContainerScreen<RequestMenu> {

    private static final ResourceLocation BACKGROUND = MiscUtil.resourceLocation("textures/gui/container/request.png");

    private EditBox searchBar;

    public RequestScreen(RequestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.inventoryLabelY = -100;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBar = new EditBox(this.font, this.leftPos + 21, this.topPos + 22, 146, 12, Component.translatable("container.classicpipes.search"));
        this.searchBar.setCanLoseFocus(false);
        this.searchBar.setMaxLength(32);
        this.searchBar.setValue(this.menu.getSearch());
        this.searchBar.setTextColor(-1);
        this.searchBar.setTextColorUneditable(-1);
        this.searchBar.setBordered(false);
        this.searchBar.setResponder(this.menu::setSearch);
        this.addRenderableWidget(this.searchBar);
        this.searchBar.setEditable(true);
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

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBar);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        return this.searchBar.keyPressed(keyCode, scanCode, modifiers) || this.searchBar.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
    }

}
