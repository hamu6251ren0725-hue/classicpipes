package jagm.classicpipes.inventory;

import jagm.classicpipes.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class SmallerCheckbox extends AbstractButton {

    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = MiscUtil.resourceLocation("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = MiscUtil.resourceLocation("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = MiscUtil.resourceLocation("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = MiscUtil.resourceLocation("widget/checkbox");
    public static final int SIZE = 11;

    private boolean selected;
    private final SmallerCheckbox.OnValueChange onValueChange;

    public SmallerCheckbox(int x, int y, SmallerCheckbox.OnValueChange onValueChange, boolean selected) {
        super(x, y, 0, 0, CommonComponents.EMPTY);
        this.width = SIZE;
        this.height = SIZE;
        this.selected = selected;
        this.onValueChange = onValueChange;
    }

    public static SmallerCheckbox.Builder builder() {
        return new SmallerCheckbox.Builder();
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }

    @Override
    public void renderWidget(GuiGraphics graphics, int x, int y, float f) {
        ResourceLocation resourcelocation;
        if (this.selected()) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX(), this.getY(), SIZE, SIZE, ARGB.white(this.alpha));
    }

    public interface OnValueChange {
        SmallerCheckbox.OnValueChange NOP = (checkbox, value) -> {};
        void onValueChange(SmallerCheckbox checkbox, boolean value);
    }

    public static class Builder {
        private int x = 0;
        private int y = 0;
        private SmallerCheckbox.OnValueChange onValueChange;
        private Tooltip tooltip;
        private boolean selected = false;

        Builder() {
            this.onValueChange = SmallerCheckbox.OnValueChange.NOP;
            this.tooltip = null;
        }

        public SmallerCheckbox.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public SmallerCheckbox.Builder onValueChange(SmallerCheckbox.OnValueChange onValueChange) {
            this.onValueChange = onValueChange;
            return this;
        }

        public SmallerCheckbox.Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public SmallerCheckbox.Builder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public SmallerCheckbox build() {
            SmallerCheckbox checkbox = new SmallerCheckbox(this.x, this.y, this.onValueChange, this.selected);
            checkbox.setTooltip(this.tooltip);
            return checkbox;
        }
    }

}
