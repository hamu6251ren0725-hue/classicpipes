package jagm.classicpipes;

import com.mojang.serialization.Codec;
import jagm.classicpipes.block.*;
import jagm.classicpipes.blockentity.*;
import jagm.classicpipes.inventory.menu.*;
import jagm.classicpipes.item.ModLabelItem;
import jagm.classicpipes.item.TagLabelItem;
import jagm.classicpipes.network.*;
import jagm.classicpipes.services.Services;
import jagm.classicpipes.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
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
    public static final Block COPPER_PIPE = createPipe("copper_pipe", CopperPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("copper_pipe"));
    public static final Block IRON_PIPE = createPipe("iron_pipe", IronPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("iron_pipe"));
    public static final Block LAPIS_PIPE = createPipe("lapis_pipe", LapisPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER_BULB).destroyTime(0.25F), translateDesc("lapis_pipe"));
    public static final Block GOLDEN_PIPE = createPipe("golden_pipe", GoldenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("golden_pipe"));
    public static final Block DIAMOND_PIPE = createPipe("diamond_pipe", DiamondPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER_BULB).destroyTime(0.25F), translateDesc("diamond_pipe"));
    public static final Block FLINT_PIPE = createPipe("flint_pipe", FlintPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.DECORATED_POT).destroyTime(0.25F), translateDesc("flint_pipe"));
    public static final Block BRICK_PIPE = createPipe("brick_pipe", BrickPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.DECORATED_POT).destroyTime(0.25F), translateDesc("brick_pipe"));
    public static final Block OBSIDIAN_PIPE = createPipe("obsidian_pipe", ObsidianPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.DECORATED_POT).destroyTime(0.25F), translateDesc("obsidian_pipe"));
    public static final Block BONE_PIPE = createPipe("bone_pipe", BonePipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.BONE_BLOCK).destroyTime(0.25F), translateDesc("bone_pipe"));
    public static final Block ROUTING_PIPE = createPipe("routing_pipe", NetworkedPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("routing_pipe.a"), translateDesc("routing_pipe.b"));
    public static final Block PROVIDER_PIPE = createPipe("provider_pipe", ProviderPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("provider_pipe"));
    public static final Block REQUEST_PIPE = createPipe("request_pipe", RequestPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("request_pipe"));
    public static final Block STOCKING_PIPE = createPipe("stocking_pipe", StockingPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("stocking_pipe"));
    public static final Block MATCHING_PIPE = createPipe("matching_pipe", MatchingPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("matching_pipe"));
    public static final Block STORAGE_PIPE = createPipe("storage_pipe", StoragePipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("storage_pipe.a"), translateDesc("storage_pipe.b"));
    public static final Block RECIPE_PIPE = createPipe("recipe_pipe", RecipePipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER).destroyTime(0.25F), translateDesc("recipe_pipe.a"), translateDesc("recipe_pipe.b"));
    public static final Block OAK_FLUID_PIPE = createWoodenFluidPipe("oak_fluid_pipe");

    public static final BlockEntityType<RoundRobinPipeEntity> BASIC_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(RoundRobinPipeEntity::new, OAK_PIPE, SPRUCE_PIPE, BIRCH_PIPE, JUNGLE_PIPE, ACACIA_PIPE, DARK_OAK_PIPE, MANGROVE_PIPE, CHERRY_PIPE, PALE_OAK_PIPE, BAMBOO_PIPE, CRIMSON_PIPE, WARPED_PIPE, BRICK_PIPE);
    public static final BlockEntityType<GoldenPipeEntity> GOLDEN_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(GoldenPipeEntity::new, GOLDEN_PIPE);
    public static final BlockEntityType<CopperPipeEntity> COPPER_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(CopperPipeEntity::new, COPPER_PIPE);
    public static final BlockEntityType<IronPipeEntity> IRON_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(IronPipeEntity::new, IRON_PIPE);
    public static final BlockEntityType<DiamondPipeEntity> DIAMOND_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(DiamondPipeEntity::new, DIAMOND_PIPE);
    public static final BlockEntityType<FlintPipeEntity> FLINT_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(FlintPipeEntity::new, FLINT_PIPE);
    public static final BlockEntityType<LapisPipeEntity> LAPIS_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(LapisPipeEntity::new, LAPIS_PIPE);
    public static final BlockEntityType<ObsidianPipeEntity> OBSIDIAN_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(ObsidianPipeEntity::new, OBSIDIAN_PIPE);
    public static final BlockEntityType<BonePipeEntity> BONE_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(BonePipeEntity::new, BONE_PIPE);
    public static final BlockEntityType<RoutingPipeEntity> ROUTING_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(RoutingPipeEntity::new, ROUTING_PIPE);
    public static final BlockEntityType<ProviderPipeEntity> PROVIDER_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(ProviderPipeEntity::new, PROVIDER_PIPE);
    public static final BlockEntityType<RequestPipeEntity> REQUEST_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(RequestPipeEntity::new, REQUEST_PIPE);
    public static final BlockEntityType<StockingPipeEntity> STOCKING_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(StockingPipeEntity::new, STOCKING_PIPE);
    public static final BlockEntityType<MatchingPipeEntity> MATCHING_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(MatchingPipeEntity::new, MATCHING_PIPE);
    public static final BlockEntityType<StoragePipeEntity> STORAGE_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(StoragePipeEntity::new, STORAGE_PIPE);
    public static final BlockEntityType<RecipePipeEntity> RECIPE_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(RecipePipeEntity::new, RECIPE_PIPE);
    public static final BlockEntityType<FluidPipeEntity> FLUID_PIPE_ENTITY = Services.LOADER_SERVICE.createBlockEntityType(FluidPipeEntity::new, OAK_FLUID_PIPE);

    public static final Item PIPE_SLICER = createItem("pipe_slicer", Item::new, new Item.Properties().stacksTo(1), translateDesc("pipe_slicer"));
    public static final Item TAG_LABEL = createItem("tag_label", TagLabelItem::new, new Item.Properties().stacksTo(1));
    public static final Item MOD_LABEL = createItem("mod_label", ModLabelItem::new, new Item.Properties().stacksTo(1));

    public static final DataComponentType<String> LABEL_COMPONENT = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
    public static final ResourceKey<DataComponentType<?>> LABEL_COMPONENT_KEY = MiscUtil.makeKey(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), "label");

    public static final SoundEvent PIPE_EJECT_SOUND = createSoundEvent("pipe_eject");
    public static final SoundEvent PIPE_ADJUST_SOUND = createSoundEvent("pipe_adjust");
    public static final SoundEvent OBSIDIAN_PIPE_DESTROY_ITEM = createSoundEvent("obsidian_pipe_destroy_item");

    public static final CreativeModeTab PIPES_TAB = CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0).title(Component.translatable("itemGroup." + MOD_ID + ".pipes")).icon(() -> new ItemStack(COPPER_PIPE)).build();
    public static final ResourceKey<CreativeModeTab> PIPES_TAB_KEY = MiscUtil.makeKey(BuiltInRegistries.CREATIVE_MODE_TAB.key(), "pipes");

    public static final MenuType<DiamondPipeMenu> DIAMOND_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(DiamondPipeMenu::new, ClientBoundBoolPayload.STREAM_CODEC);
    public static final MenuType<RoutingPipeMenu> ROUTING_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(RoutingPipeMenu::new, ClientBoundTwoBoolsPayload.STREAM_CODEC);
    public static final MenuType<ProviderPipeMenu> PROVIDER_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(ProviderPipeMenu::new, ClientBoundTwoBoolsPayload.STREAM_CODEC);
    public static final MenuType<RequestMenu> REQUEST_MENU = Services.LOADER_SERVICE.createMenuType(RequestMenu::new, ClientBoundItemListPayload.STREAM_CODEC);
    public static final MenuType<StockingPipeMenu> STOCKING_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(StockingPipeMenu::new, ClientBoundTwoBoolsPayload.STREAM_CODEC);
    public static final MenuType<MatchingPipeMenu> MATCHING_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(MatchingPipeMenu::new, ClientBoundBoolPayload.STREAM_CODEC);
    public static final MenuType<StoragePipeMenu> STORAGE_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(StoragePipeMenu::new, ClientBoundThreeBoolsPayload.STREAM_CODEC);
    public static final MenuType<RecipePipeMenu> RECIPE_PIPE_MENU = Services.LOADER_SERVICE.createMenuType(RecipePipeMenu::new, ClientBoundRecipePipePayload.STREAM_CODEC);

    private static Item createItem(String name, Function<Item.Properties, Item> factory, Item.Properties props, Component... lore) {
        if (lore.length > 0) {
            props.component(DataComponents.LORE, new ItemLore(List.of(), Arrays.asList(lore)));
        }
        Item item = factory.apply(props.setId(MiscUtil.makeKey(Registries.ITEM, name)));
        ITEMS.put(name, item);
        return item;
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

    private static Block createWoodenPipe(String name) {
        return createPipe(name, WoodenPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING), translateDesc("wooden_pipe"));
    }

    private static Block createWoodenFluidPipe(String name) {
        return createPipe(name, WoodenFluidPipeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.SCAFFOLDING), translateDesc("wooden_fluid_pipe"));
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
