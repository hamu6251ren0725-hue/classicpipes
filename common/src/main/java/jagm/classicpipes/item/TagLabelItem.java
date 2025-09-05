package jagm.classicpipes.item;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class TagLabelItem extends Item {

    public TagLabelItem(Properties properties) {
        super(properties);
    }

    private static String labelToTranslationKey(String label) {
        String[] split = label.split(":");
        if (split.length < 2) {
            return "tag.item.minecraft." + label.replace("/", ".");
        } else {
            return "tag.item." + split[0] + "." + split[1].replace("/", ".");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        TagKey<Item> tagKey = stack.get(ClassicPipes.LABEL_COMPONENT);
        if (tagKey != null) {
            String label = tagKey.location().toString();
            MutableComponent tagTranslation = Component.translatableWithFallback(labelToTranslationKey(label), "");
            if (!tagTranslation.getString().isEmpty()) {
                tooltipAdder.accept(tagTranslation.withStyle(ChatFormatting.YELLOW));
            }
            tooltipAdder.accept(Component.literal("#" + label).withStyle(ChatFormatting.YELLOW));
        }
    }

    public static boolean itemMatches(ItemStack tagStack, ItemStack compareStack) {
        TagKey<Item> tagKey = tagStack.get(ClassicPipes.LABEL_COMPONENT);
        return tagKey != null && compareStack.is(tagKey);
    }

}
