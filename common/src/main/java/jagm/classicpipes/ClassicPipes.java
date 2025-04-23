package jagm.classicpipes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import jagm.classicpipes.block.WoodenPipeBlock;
import jagm.classicpipes.blockentity.WoodenPipeEntity;
import jagm.classicpipes.services.Services;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ClassicPipes {

    public static final String MOD_ID = "classicpipes";
    public static final String MOD_NAME = "Classic Pipes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final HashMap<String, Supplier<Item>> ITEMS = new HashMap<>();
    public static final HashMap<String, Supplier<Block>> BLOCKS = new HashMap<>();
    public static final HashMap<String, Supplier<BlockEntityType<? extends BlockEntity>>> BLOCK_ENTITIES = new HashMap<>();

    public static final List<Block> TRANSPARENT_BLOCKS = new ArrayList<>();
    private static final List<Block> WOODEN_PIPES = new ArrayList<>();

    public static final Supplier<Block> OAK_PIPE = createWoodenPipeSupplier("oak_pipe");
    /*
    public static final Supplier<Block> SPRUCE_PIPE = createWoodenPipeSupplier("spruce_pipe");
    public static final Supplier<Block> BIRCH_PIPE = createWoodenPipeSupplier("birch_pipe");
    public static final Supplier<Block> JUNGLE_PIPE = createWoodenPipeSupplier("jungle_pipe");
    public static final Supplier<Block> ACACIA_PIPE = createWoodenPipeSupplier("acacia_pipe");
    public static final Supplier<Block> DARK_OAK_PIPE = createWoodenPipeSupplier("dark_oak_pipe");
    public static final Supplier<Block> MANGROVE_PIPE = createWoodenPipeSupplier("mangrove_pipe");
    public static final Supplier<Block> CHERRY_PIPE = createWoodenPipeSupplier("cherry_pipe");
    public static final Supplier<Block> PALE_OAK_PIPE = createWoodenPipeSupplier("pale_oak_pipe");
    public static final Supplier<Block> BAMBOO_PIPE = createWoodenPipeSupplier("bamboo_pipe");
    public static final Supplier<Block> CRIMSON_PIPE = createWoodenPipeSupplier("crimson_pipe");
    public static final Supplier<Block> WARPED_PIPE = createWoodenPipeSupplier("warped_pipe");
     */

    public static final Supplier<BlockEntityType<? extends BlockEntity>> WOODEN_PIPE_ENTITY = Suppliers.memoize(Services.BLOCK_ENTITY_HELPER.getBlockEntitySupplier(
            WoodenPipeEntity::new, WOODEN_PIPES.toArray(new Block[0])
    ));

    private static <T> ResourceKey<T> makeKey(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    private static Supplier<Item> createItemSupplier(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        Supplier<Item> itemSupplier = Suppliers.memoize(() -> factory.apply(props.setId(makeKey(Registries.ITEM, name))));
        ITEMS.put(name, itemSupplier);
        return itemSupplier;
    }

    private static Supplier<Block> createBlockSupplier(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Supplier<Block> blockSupplier = Suppliers.memoize(() -> factory.apply(props.setId(makeKey(Registries.BLOCK, name))));
        BLOCKS.put(name, blockSupplier);
        createItemSupplier(name, itemProps -> new BlockItem(blockSupplier.get(), itemProps), new Item.Properties());
        return blockSupplier;
    }

    private static Supplier<Block> createPipeSupplier(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Supplier<Block> pipeSupplier = createBlockSupplier(name, factory, props);
        TRANSPARENT_BLOCKS.add(pipeSupplier.get());
        return pipeSupplier;
    }

    private static Supplier<Block> createWoodenPipeSupplier(String name) {
        Supplier<Block> woodenPipeSupplier = createPipeSupplier(name, WoodenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING));
        WOODEN_PIPES.add(woodenPipeSupplier.get());
        return woodenPipeSupplier;
    }

    static {
        BLOCK_ENTITIES.put("wooden_pipe", WOODEN_PIPE_ENTITY);
    }

}
