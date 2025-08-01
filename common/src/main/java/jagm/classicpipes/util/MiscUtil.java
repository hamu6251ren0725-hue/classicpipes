package jagm.classicpipes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.AbstractPipeBlock;
import jagm.classicpipes.services.Services;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class MiscUtil {

    public static final boolean DEBUG_MODE = true;
    public static final Comparator<ItemStack> AMOUNT = Comparator.comparing(ItemStack::getCount);
    public static final Comparator<ItemStack> NAME = Comparator.comparing(stack -> stack.getItem().getName().getString());
    public static final Comparator<ItemStack> MOD = Comparator.comparing(stack -> Services.LOADER_SERVICE.getModName(MiscUtil.modFromItem(stack)));
    public static final Comparator<ItemStack> CRAFTABLE = Comparator.comparing(stack -> stack.getCount() == 0 ? 1 : 0);

    public static final Codec<ItemStack> UNLIMITED_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Item.CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
            Codec.INT.fieldOf("count").forGetter(ItemStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
    ).apply(instance, ItemStack::new));

    public static ResourceLocation resourceLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(ClassicPipes.MOD_ID, name);
    }

    public static <T> ResourceKey<T> makeKey(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, resourceLocation(name));
    }

    public static Direction nextDirection(Direction direction) {
        return Direction.from3DDataValue((direction.get3DDataValue() + 1) % 6);
    }

    public static boolean itemIsPipe(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof AbstractPipeBlock;
        }
        return false;
    }

    public static String modFromItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().split(":")[0];
    }

}
