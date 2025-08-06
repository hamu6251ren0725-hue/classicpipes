package jagm.classicpipes.client.screen;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.inventory.menu.CraftingPipeMenu;
import jagm.classicpipes.network.ServerBoundSlotDirectionPayload;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CraftingPipeScreen extends AbstractContainerScreen<CraftingPipeMenu> {

    private static final ResourceLocation BACKGROUND = MiscUtil.resourceLocation("textures/gui/container/crafting_pipe.png");
    private static final ChatFormatting[] DIRECTION_COLOURS = new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE, ChatFormatting.GREEN, ChatFormatting.YELLOW, ChatFormatting.BLUE, ChatFormatting.WHITE, ChatFormatting.RED};

    private final Button[] slotDirectionButtons;

    public CraftingPipeScreen(CraftingPipeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.slotDirectionButtons = new Button[10];
        this.imageHeight = 189;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int slot = j + i * 3;
                Direction slotDirection = this.menu.getSlotDirection(slot);
                this.slotDirectionButtons[slot] = Button.builder(
                        Component.translatable("direction." + ClassicPipes.MOD_ID + ".short." + slotDirection.name().toLowerCase()).withStyle(DIRECTION_COLOURS[slotDirection.get3DDataValue()]),
                        button -> this.cycleSlotDirection(button, slot)
                )
                        .bounds(this.leftPos + 9 + j * 12, this.topPos + 25 + i * 12, 12, 12)
                        .tooltip(Tooltip.create(Component.translatable("tooltip." + ClassicPipes.MOD_ID + ".crafting_pipe_grid")))
                        .build();
            }
        }
        Direction slotDirection = this.menu.getSlotDirection(9);
        this.slotDirectionButtons[9] = Button.builder(
                Component.translatable("direction." + ClassicPipes.MOD_ID + ".short." + slotDirection.name().toLowerCase()).withStyle(DIRECTION_COLOURS[slotDirection.get3DDataValue()]),
                button -> this.cycleSlotDirection(button, 9)
        )
                .bounds(this.leftPos + 149, this.topPos + 37, 12, 12)
                .tooltip(Tooltip.create(Component.translatable("tooltip." + ClassicPipes.MOD_ID + ".crafting_pipe_result")))
                .build();
        for (Button button : this.slotDirectionButtons) {
            this.addRenderableWidget(button);
        }
    }

    private void cycleSlotDirection(Button button, int slot) {
        Direction newDirection = hasShiftDown() ? MiscUtil.prevDirection(this.menu.getSlotDirection(slot)) : MiscUtil.nextDirection(this.menu.getSlotDirection(slot));
        this.menu.setSlotDirection(slot, newDirection);
        button.setMessage(Component.translatable("direction." + ClassicPipes.MOD_ID + ".short." + newDirection.name().toLowerCase()).withStyle(DIRECTION_COLOURS[newDirection.get3DDataValue()]));
        Services.LOADER_SERVICE.sendToServer(new ServerBoundSlotDirectionPayload(this.menu.getPos(), slot, newDirection));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2 - 9, this.titleLabelY, -12566464, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }

}
