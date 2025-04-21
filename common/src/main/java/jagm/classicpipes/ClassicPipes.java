package jagm.classicpipes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import jagm.classicpipes.block.WoodenPipeBlock;
import jagm.classicpipes.blockentity.WoodenPipeEntity;
import jagm.classicpipes.services.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.Function;

public class ClassicPipes {

    public static final String MOD_ID = "classicpipes";
    public static final String MOD_NAME = "Classic Pipes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final HashMap<String, Supplier<Item>> ITEMS = new HashMap<>();
    public static final HashMap<String, Supplier<Block>> BLOCKS = new HashMap<>();

    public static final Supplier<Block> WOODEN_PIPE = createBlockSupplier(
            "wooden_pipe",
            WoodenPipeBlock::new,
            BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING)
    );

    public static final BlockEntityType<WoodenPipeEntity> WOODEN_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.registerBlockEntityType(
            "wooden_pipe",
            WoodenPipeEntity::new,
            WOODEN_PIPE.get()
    );

    private static <T> ResourceKey<T> makeKey(ResourceKey<? extends Registry<T>> registry, String name){
        return ResourceKey.create(registry, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    private static Supplier<Item> createItemSupplier(String name, Function<Item.Properties, Item> factory, Item.Properties props){
        Supplier<Item> itemSupplier = Suppliers.memoize(() -> factory.apply(props.setId(makeKey(Registries.ITEM, name))));
        ITEMS.put(name, itemSupplier);
        return itemSupplier;
    }

    private static Supplier<Block> createBlockSupplier(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props){
        Supplier<Block> blockSupplier = Suppliers.memoize(() -> factory.apply(props.setId(makeKey(Registries.BLOCK, name))));
        BLOCKS.put(name, blockSupplier);
        createItemSupplier(name, itemProps -> new BlockItem(blockSupplier.get(), itemProps), new Item.Properties());
        return blockSupplier;
    }

}
