package jagm.classicpipes;

import jagm.classicpipes.block.*;
import jagm.classicpipes.blockentity.*;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    public static final Block COPPER_PIPE = createPipe("copper_pipe", CopperPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER));
    public static final Block IRON_PIPE = createPipe("iron_pipe", IronPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER));
    public static final Block DIAMOND_PIPE = createPipe("diamond_pipe", DiamondPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER_BULB));

    public static final BlockEntityType<RoundRobinPipeEntity> WOODEN_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(RoundRobinPipeEntity::new, WOODEN_PIPES.toArray(new Block[0]));
    public static final BlockEntityType<GoldenPipeEntity> GOLDEN_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(GoldenPipeEntity::new, GOLDEN_PIPE);
    public static final BlockEntityType<CopperPipeEntity> COPPER_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(CopperPipeEntity::new, COPPER_PIPE);
    public static final BlockEntityType<IronPipeEntity> IRON_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(IronPipeEntity::new, IRON_PIPE);
    public static final BlockEntityType<DiamondPipeEntity> DIAMOND_PIPE_ENTITY = Services.BLOCK_ENTITY_HELPER.createBlockEntityType(DiamondPipeEntity::new, DIAMOND_PIPE);

    public static final SoundEvent PIPE_EJECT_SOUND = createSoundEvent("pipe_eject");
    public static final SoundEvent PIPE_ADJUST_SOUND = createSoundEvent("pipe_adjust");

    public static final CreativeModeTab PIPES_TAB = CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0).title(Component.translatable("itemGroup." + MOD_ID + ".pipes")).icon(() -> new ItemStack(COPPER_PIPE)).build();
    public static final ResourceKey<CreativeModeTab> PIPES_TAB_KEY = MiscUtil.makeKey(BuiltInRegistries.CREATIVE_MODE_TAB.key(), "pipes");

    private static void createItem(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        Item item = factory.apply(props.setId(MiscUtil.makeKey(Registries.ITEM, name)));
        ITEMS.put(name, item);
    }

    private static Block createBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
        Block block = factory.apply(props.setId(MiscUtil.makeKey(Registries.BLOCK, name)));
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

    private static SoundEvent createSoundEvent(String name) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(MiscUtil.resourceLocation(name));
        SOUNDS.put(name, soundEvent);
        return soundEvent;
    }

}
