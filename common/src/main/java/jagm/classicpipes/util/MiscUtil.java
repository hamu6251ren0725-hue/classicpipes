package jagm.classicpipes.util;

import jagm.classicpipes.ClassicPipes;
import jagm.classicpipes.block.AbstractPipeBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class MiscUtil {

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

}
