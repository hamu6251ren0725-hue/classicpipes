package jagm.classicpipes;

import jagm.classicpipes.block.GoldenPipeBlock;
import jagm.classicpipes.block.WoodenPipeBlock;
import jagm.classicpipes.blockentity.GoldenPipeEntity;
import jagm.classicpipes.blockentity.StandardPipeEntity;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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

    public static final HashMap<String, Item> ITEMS = new HashMap<>();
    public static final HashMap<String, Block> BLOCKS = new HashMap<>();
    public static final HashMap<String, SoundEvent> SOUNDS = new HashMap<>();

    public static final List<Block> TRANSPARENT_BLOCKS = new ArrayList<>();
    private static final List<Block> WOODEN_PIPES = new ArrayList<>();

    public static final Block OAK_PIPE = createWoodenPipe("oak_pipe");
    public static final Block SPRUCE_PIPE = createWoodenPipe("spruce_pipe");
    public static final Block BIRCH_PIPE = createWoodenPipe("birch_pipe");
    public static final Block JUNGLE_PIPE = createWoodenPipe("jungle_pipe");
    public static final Block ACACIA_PIPE = createWoodenPipe("acacia_pipe");
    public static final Block DARK_OAK_PIPE = createWoodenPipe("dark_oak_pipe");
    public static final Block MANGROVE_PIPE = createWoodenPipe("mangrove_pipe");
    public static final Block CHERRY_PIPE = createWoodenPipe("cherry_pipe");
    public static final Block PALE_OAK_PIPE = createWoodenPipe("pale_oak_pipe");
    public static final Block BAMBOO_PIPE = createWoodenPipe("bamboo_pipe");
    public static final Block CRIMSON_PIPE = createWoodenPipe("crimson_pipe");
    public static final Block WARPED_PIPE = createWoodenPipe("warped_pipe");

    public static final Block GOLDEN_PIPE = createPipe("golden_pipe", GoldenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER));

    public static final BlockEntityType<StandardPipeEntity> WOODEN_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(StandardPipeEntity::new, WOODEN_PIPES.toArray(new Block[0]));
    public static final BlockEntityType<StandardPipeEntity> GOLDEN_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(GoldenPipeEntity::new, GOLDEN_PIPE);

    public static final SoundEvent PIPE_EJECT_SOUND = createSoundEvent("pipe_eject");

    private static <T> ResourceKey<T> makeKey(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, MiscUtil.resourceLocation(name));
    }

    private static Item createItem(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        Item item = factory.apply(props.setId(makeKey(Registries.ITEM, name)));
        ITEMS.put(name, item);
        return item;
    }

    private static Block createBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Block block = factory.apply(props.setId(makeKey(Registries.BLOCK, name)));
        BLOCKS.put(name, block);
        createItem(name, itemProps -> new BlockItem(block, itemProps), new Item.Properties());
        return block;
    }

    private static Block createPipe(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Block pipe = createBlock(name, factory, props.noOcclusion());
        TRANSPARENT_BLOCKS.add(pipe);
        return pipe;
    }

    private static Block createWoodenPipe(String name) {
        Block woodenPipe = createPipe(name, WoodenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING));
        WOODEN_PIPES.add(woodenPipe);
        return woodenPipe;
    }

    private static SoundEvent createSoundEvent (String name) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(MiscUtil.resourceLocation(name));
        SOUNDS.put(name, soundEvent);
        return soundEvent;
    }

}
