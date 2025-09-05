package jagm.classicpipes.item;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class TagLabelItem extends Item {

    public TagLabelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack targetStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        List<TagKey<Item>> tags = targetStack.getTags().toList();
        if (targetStack.isEmpty()) {
            if (level.isClientSide()) {
                player.displayClientMessage(Component.translatable("chat." + ClassicPipes.MOD_ID + ".nothing_in_offhand"), false);
            }
        } else if (tags.isEmpty()) {
            if (level.isClientSide()) {
                player.displayClientMessage(Component.translatable("chat." + ClassicPipes.MOD_ID + ".no_tags_in_hand"), false);
            }
        } else {
            ItemStack labelStack = player.getItemInHand(hand);
            TagKey<Item> currentTag = labelStack.get(ClassicPipes.LABEL_COMPONENT);
            if (currentTag == null || !tags.contains(currentTag)) {
                labelStack.set(ClassicPipes.LABEL_COMPONENT, tags.getFirst());
                if (level.isClientSide()) {
                    player.displayClientMessage(tagSetMessage(tags.getFirst()), false);
                }
            } else {
                for (int i = 0; i < tags.size(); i++) {
                    if (tags.get(i).equals(currentTag)) {
                        TagKey<Item> tag = tags.get((i + 1) % tags.size());
                        labelStack.set(ClassicPipes.LABEL_COMPONENT, tag);
                        if (level.isClientSide()) {
                            player.displayClientMessage(tagSetMessage(tag), false);
                        }
                        break;
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private Component tagSetMessage(TagKey<Item> tag) {
        MutableComponent tagTranslation = Component.translatableWithFallback(labelToTranslationKey(tag.location().toString()), "");
        if (tagTranslation.getString().isEmpty()) {
            return Component.translatable("chat." + ClassicPipes.MOD_ID + ".tag_set", Component.literal("#" + tag.location().toString()).withStyle(ChatFormatting.YELLOW));
        } else {
            return Component.translatable("chat." + ClassicPipes.MOD_ID + ".tag_set_translatable", Component.literal("#" + tag.location().toString()).withStyle(ChatFormatting.YELLOW), tagTranslation.withStyle(ChatFormatting.YELLOW));
        }
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
        TagKey<Item> tag = stack.get(ClassicPipes.LABEL_COMPONENT);
        if (tag != null) {
            String label = tag.location().toString();
            MutableComponent tagTranslation = Component.translatableWithFallback(labelToTranslationKey(label), "");
            if (!tagTranslation.getString().isEmpty()) {
                tooltipAdder.accept(tagTranslation.withStyle(ChatFormatting.YELLOW));
            }
            tooltipAdder.accept(Component.literal("#" + label).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltipAdder.accept(Component.translatable("item." + ClassicPipes.MOD_ID + ".tag_label.desc").withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean itemMatches(ItemStack tagStack, ItemStack compareStack) {
        TagKey<Item> tag = tagStack.get(ClassicPipes.LABEL_COMPONENT);
        return tag != null && compareStack.is(tag);
    }

}
