package jagm.classicpipes.client.screen;

import jagm.classicpipes.inventory.menu.RequestMenu;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RequestScreen extends AbstractContainerScreen<RequestMenu> {

    private static final ResourceLocation BACKGROUND = MiscUtil.resourceLocation("textures/gui/container/request.png");

    private EditBox searchBar;

    public RequestScreen(RequestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
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
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        ItemStack stack = slot.getItem();
        int seed = slot.x + slot.y * this.imageWidth;
        graphics.renderItem(stack, slot.x, slot.y, seed);
        if (!stack.isEmpty()) {
            graphics.pose().pushMatrix();
            this.renderItemBar(graphics, stack, slot.x, slot.y);
            this.renderItemCount(graphics, this.font, stack, slot.x, slot.y);
            graphics.pose().popMatrix();
        }
    }

    private void renderItemBar(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isBarVisible()) {
            int i = x + 2;
            int j = y + 13;
            graphics.fill(RenderPipelines.GUI, i, j, i + 13, j + 2, -16777216);
            graphics.fill(RenderPipelines.GUI, i, j, i + stack.getBarWidth(), j + 1, ARGB.opaque(stack.getBarColor()));
        }
    }

    private void renderItemCount(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        int count = stack.getCount();
        if (count != 1) {
            String s = stringForCount(count);
            float countScale = 1.0F;
            if (this.minecraft != null) {
                final int guiScale = this.minecraft.getWindow().getGuiScale();
                int numerator = guiScale;
                while (font.width(s) * countScale > 16 && numerator > 1) {
                    numerator--;
                    countScale = (float) numerator / guiScale;
                }
            }
            int slotOffset = countScale == 1.0F ? 17 : 16;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + slotOffset, y + slotOffset);
            graphics.pose().scale(countScale);
            graphics.drawString(font, s, -font.width(s), -8, -1, true);
            graphics.pose().popMatrix();
        }

    }

    private static String stringForCount(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 10000) {
            return String.format("%.1f", (float) count / 1000 - 0.049F) + "K";
        } else if (count < 1000000) {
            return count / 1000 + "K";
        } else if (count < 10000000) {
            return String.format("%.1f", (float) count / 1000000 - 0.049F) + "M";
        } else if (count < 1000000000) {
            return count / 1000000 + "M";
        } else if (count < Integer.MAX_VALUE) {
            return String.format("%.1f", (float) count / 1000000000 - 0.049F) + "B";
        } else {
            return "MAX";
        }
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
