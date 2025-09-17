package jagm.classicpipes.client.screen;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.client.renderer.FluidRenderInfo;
import jagm.classicpipes.inventory.container.Filter;
import jagm.classicpipes.inventory.menu.DiamondFluidPipeMenu;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class DiamondFluidPipeScreen extends FilterScreen<DiamondFluidPipeMenu> {

    private static final ResourceLocation BACKGROUND = MiscUtil.resourceLocation("textures/gui/container/diamond_pipe.png");

    public DiamondFluidPipeScreen(DiamondFluidPipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 240;
        this.imageHeight = 236;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - 176) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        for (int i = 0; i < 6; i++) {
            Component text = Component.translatable("direction." + ClassicPipes.MOD_ID + "." + Direction.from3DDataValue(i).name().toLowerCase()).withStyle(ChatFormatting.BLACK);
            graphics.drawString(this.font, text, this.leftPos - 30 + (35 - this.font.width(text)) / 2, this.topPos + 22 + 18 * i, -12566464, false);
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - 176) / 2 - 64;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot.container instanceof Filter && slot.hasItem()) {
            Fluid fluid = Services.LOADER_SERVICE.getFluidFromStack(slot.getItem());
            if (fluid != null) {
                FluidRenderInfo info = Services.LOADER_SERVICE.getFluidRenderInfo(fluid.defaultFluidState());
                graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ARGB.opaque(info.tint()));
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, info.sprite(), slot.x, slot.y, 16, 16, info.tint());
                return;
            }
        }
        super.renderSlot(graphics, slot);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            if (this.menu.getCarried().isEmpty() || stack.getTooltipImage().map(ClientTooltipComponent::create).map(ClientTooltipComponent::showTooltipWithItemInHand).orElse(false)) {
                if (this.hoveredSlot.container instanceof Filter) {
                    Fluid fluid = Services.LOADER_SERVICE.getFluidFromStack(stack);
                    if (fluid != null) {
                        graphics.setTooltipForNextFrame(this.font, Services.LOADER_SERVICE.getFluidName(fluid), mouseX, mouseY);
                        return;
                    }
                }
                graphics.setTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(stack), stack.getTooltipImage(), mouseX, mouseY, stack.get(DataComponents.TOOLTIP_STYLE));
            }
        }
    }

}
