package jagm.classicpipes;

import jagm.classicpipes.block.*;
import jagm.classicpipes.blockentity.*;
import jagm.classicpipes.inventory.menu.DiamondPipeMenu;
import jagm.classicpipes.inventory.menu.NetheriteBasicPipeMenu;
import jagm.classicpipes.network.ClientBoundNetheritePipePayload;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class ClassicPipes {

    public static final String MOD_ID = "classicpipes";
    public static final String MOD_NAME = "Classic Pipes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, SoundEvent> SOUNDS = new LinkedHashMap<>();

    public static final List<Block> TRANSPARENT_BLOCKS = new ArrayList<>();
    private static final List<Block> BASIC_PIPES = new ArrayList<>();

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
    public static final Block COPPER_PIPE = createPipe("copper_pipe", CopperPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER), translateDesc("copper_pipe"));
    public static final Block IRON_PIPE = createPipe("iron_pipe", IronPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER), translateDesc("iron_pipe"));
    public static final Block LAPIS_PIPE = createPipe("lapis_pipe", LapisPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER_BULB), translateDesc("lapis_pipe"));
    public static final Block GOLDEN_PIPE = createPipe("golden_pipe", GoldenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER), translateDesc("golden_pipe"));
    public static final Block DIAMOND_PIPE = createPipe("diamond_pipe", DiamondPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER_BULB), translateDesc("diamond_pipe"));
    public static final Block FLINT_PIPE = createPipe("flint_pipe", FlintPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.DECORATED_POT), translateDesc("flint_pipe"));
    public static final Block BRICK_PIPE = createBasicPipe("brick_pipe", BrickPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.DECORATED_POT), translateDesc("brick_pipe"));
    public static final Block OBSIDIAN_PIPE = createPipe("obsidian_pipe", ObsidianPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.DECORATED_POT), translateDesc("obsidian_pipe"));
    public static final Block NETHERITE_BASIC_PIPE = createPipe("netherite_pipe", NetheritePipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER), translateDesc("netherite_pipe.a"), translateDesc("netherite_pipe.b"));
    public static final Block PROVIDER_PIPE = createPipe("provider_pipe", ProviderPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER), translateDesc("provider_pipe"));

    public static final BlockEntityType<RoundRobinPipeEntity> BASIC_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(RoundRobinPipeEntity::new, BASIC_PIPES.toArray(new Block[0]));
    public static final BlockEntityType<GoldenPipeEntity> GOLDEN_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(GoldenPipeEntity::new, GOLDEN_PIPE);
    public static final BlockEntityType<CopperPipeEntity> COPPER_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(CopperPipeEntity::new, COPPER_PIPE);
    public static final BlockEntityType<IronPipeEntity> IRON_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(IronPipeEntity::new, IRON_PIPE);
    public static final BlockEntityType<DiamondPipeEntity> DIAMOND_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(DiamondPipeEntity::new, DIAMOND_PIPE);
    public static final BlockEntityType<FlintPipeEntity> FLINT_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(FlintPipeEntity::new, FLINT_PIPE);
    public static final BlockEntityType<LapisPipeEntity> LAPIS_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(LapisPipeEntity::new, LAPIS_PIPE);
    public static final BlockEntityType<ObsidianPipeEntity> OBSIDIAN_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(ObsidianPipeEntity::new, OBSIDIAN_PIPE);
    public static final BlockEntityType<NetheriteBasicPipeEntity> NETHERITE_BASIC_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(NetheriteBasicPipeEntity::new, NETHERITE_BASIC_PIPE);
    public static final BlockEntityType<ProviderPipeEntity> PROVIDER_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(ProviderPipeEntity::new, PROVIDER_PIPE);

    public static final SoundEvent PIPE_EJECT_SOUND = createSoundEvent("pipe_eject");
    public static final SoundEvent PIPE_ADJUST_SOUND = createSoundEvent("pipe_adjust");
    public static final SoundEvent OBSIDIAN_PIPE_DESTROY_ITEM = createSoundEvent("obsidian_pipe_destroy_item");

    public static final CreativeModeTab PIPES_TAB = CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0).title(Component.translatable("itemGroup." + MOD_ID + ".pipes")).icon(() -> new ItemStack(COPPER_PIPE)).build();
    public static final ResourceKey<CreativeModeTab> PIPES_TAB_KEY = MiscUtil.makeKey(BuiltInRegistries.CREATIVE_MODE_TAB.key(), "pipes");

    public static final MenuType<DiamondPipeMenu> DIAMOND_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(DiamondPipeMenu::new, ByteBufCodecs.BOOL);
    public static final MenuType<NetheriteBasicPipeMenu> NETHERITE_BASIC_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(NetheriteBasicPipeMenu::new, ClientBoundNetheritePipePayload.STREAM_CODEC);

    private static void createItem(String name, Function<Item.Properties, Item> factory, Item.Properties props, Component... lore) {
        if (lore.length > 0) {
            props.component(DataComponents.LORE, new ItemLore(List.of(), Arrays.asList(lore)));
        }
        Item item = factory.apply(props.setId(MiscUtil.makeKey(Registries.ITEM, name)));
        ITEMS.put(name, item);
    }

    private static Block createBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props, boolean fashionable, Component... lore) {
        Block block = factory.apply(props.setId(MiscUtil.makeKey(Registries.BLOCK, name)));
        BLOCKS.put(name, block);
        createItem(name, itemProps -> new BlockItem(block, itemProps), fashionable ? new Item.Properties().equippableUnswappable(EquipmentSlot.HEAD) : new Item.Properties(), lore);
        return block;
    }

    private static Block createPipe(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props, Component... lore) {
        Block pipe = createBlock(name, factory, props.noOcclusion(), true, lore);
        TRANSPARENT_BLOCKS.add(pipe);
        return pipe;
    }

    private static Block createBasicPipe(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props, Component... lore) {
        Block pipe = createPipe(name, factory, props, lore);
        BASIC_PIPES.add(pipe);
        return pipe;
    }

    private static Block createWoodenPipe(String name) {
        return createBasicPipe(name, WoodenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING), translateDesc("wooden_pipe"));
    }

    private static SoundEvent createSoundEvent(String name) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(MiscUtil.resourceLocation(name));
        SOUNDS.put(name, soundEvent);
        return soundEvent;
    }

    private static Component translateDesc(String desc) {
        return Component.translatable("item." + MOD_ID + "." + desc + ".desc").withStyle(ChatFormatting.GRAY);
    }

}
